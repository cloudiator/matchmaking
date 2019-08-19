package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkNotNull;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.DiscoveryItemState;
import cloudiator.Environment;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.Price;
import cloudiator.Runtime;
import com.typesafe.config.Config;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.matchmaking.ocl.ByonUpdater.ByonTriple;
import org.cloudiator.matchmaking.ocl.DefaultNodeGenerator.PriceCache.PriceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultNodeGenerator implements NodeGenerator {

  private static final Config config = Configuration.conf().getConfig("matchmaking.nodeGenerator");
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNodeGenerator.class);
  private static final PriceCache PRICE_CACHE = new PriceCache();
  private final NodeCandidateFactory nodeCandidateFactory;
  private final CloudiatorModel cloudiatorModel;
  @Nullable
  private final ByonUpdater byonUpdater;

  public DefaultNodeGenerator(NodeCandidateFactory nodeCandidateFactory,
      CloudiatorModel cloudiatorModel, @Nullable ByonUpdater byonUpdater) {
    this.nodeCandidateFactory = nodeCandidateFactory;
    this.cloudiatorModel = cloudiatorModel;
    if (!PRICE_CACHE.exists(cloudiatorModel)) {
      PRICE_CACHE.load(cloudiatorModel);
    }
    this.byonUpdater = byonUpdater;
  }

  private static boolean isValidCombination(Image image, Hardware hardware, Location location) {
    checkNotNull(hardware, "hardware is null");
    checkNotNull(image, "image is null");
    checkNotNull(location, "location is null");

    //we have a valid combination if the location is assignable
    //and is in the scope of the image and the hardware

    if (!location.isAssignable()) {
      //if the location is not assignable, the combination is always invalid
      return false;
    }

    final Set<String> locationScope = locationIds(location);

    if (image.getLocation() != null) {
      //we have to check the image scope
      if (!locationScope.contains(image.getLocation().getId())) {
        return false;
      }
    }

    if (hardware.getLocation() != null) {
      //we have to check the hardware scope
      if (!locationScope.contains(hardware.getLocation().getId())) {
        return false;
      }
    }

    return true;
  }

  private static Set<String> locationIds(Location location) {
    Set<String> ids = new HashSet<>();
    for (Location i = location; i != null; i = i.getParent()) {
      ids.add(i.getId());
    }
    return ids;
  }

  /*
   * (non-Javadoc)
   *
   * @see ocl.NodeGenerator#getPossibleNodes()
   */
  @Override
  public NodeCandidates get() {
    Set<NodeCandidate> nodeCandidates = new HashSet<>();
    for (Cloud cloud : cloudiatorModel.getClouds()) {
      for (Image image : cloud.getImages()) {
        for (Hardware hardware : cloud.getHardwareList()) {
          for (Location location : cloud.getLocations()) {
            //check if valid combination
            if (isValidCombination(image, hardware, location)) {
              Double price = PRICE_CACHE
                  .retrieve(cloudiatorModel, PriceKey.of(cloud, image, hardware, location));
              if (price == null) {
                price = Double.MAX_VALUE;
              }
              if (!price.equals(Double.MAX_VALUE)) {
                nodeCandidates
                    .add(nodeCandidateFactory.of(cloud, hardware, image, location, price));
              }
            }
          }
        }
      }
      try {
        if (config.hasPath(cloud.getApi().getProviderName())) {
          nodeCandidates.addAll(generateFaasNodeCandidates(cloud));
        }
      } catch (Exception e) {
        LOGGER.trace("Exception while generating faas candidates. Ignoring.", e);
      }
    }
    nodeCandidates.addAll(generateByonNodeCandidates());
    System.out
        .println(String.format("%s generated all possible nodes: %s", this, nodeCandidates.size()));
    return NodeCandidates.of(nodeCandidates);
  }

  private Set<NodeCandidate> generateByonNodeCandidates() {

    if (byonUpdater == null) {
      return Collections.emptySet();
    }

    Set<NodeCandidate> nodeCandidates = new HashSet<>();
    Set<ByonTriple> triples = byonUpdater.getValidTriples();

    for (ByonTriple triple : triples) {
      nodeCandidates.add(nodeCandidateFactory.byon(triple.hardware, triple.image, triple.location));
    }

    return nodeCandidates;
  }

  private Set<NodeCandidate> generateFaasNodeCandidates(Cloud cloud) {
    Set<NodeCandidate> nodeCandidates = new HashSet<>();
    // Get cloud-specific properties
    Config cloudConfig = config.getConfig(cloud.getApi().getProviderName());
    int memMin = cloudConfig.getInt("memoryMin");
    int memMax = cloudConfig.getInt("memoryMax");
    int memInc = cloudConfig.getInt("memoryIncrement");
    List<String> runtimes = cloudConfig.getStringList("runtimes");
    // Get every combination of location/memory/runtime
    for (Location location : cloud.getLocations()) {
      for (int memory = memMin; memory <= memMax; memory += memInc) {
        for (String runtime : runtimes) {
          // Build hardware object
          Hardware hardware = CloudiatorFactory.eINSTANCE.createHardware();
          hardware.setId(cloud.getId() + memory);
          hardware.setName(String.format("%s-%s", cloud.getApi().getProviderName(), memory));
          hardware.setProviderId(hardware.getName());
          hardware.setRam(memory);
          hardware.setCores(1);
          hardware.setDisk(512.);
          hardware.setState(DiscoveryItemState.OK);
          hardware.setOwner(cloud.getOwner());

          Environment environment = CloudiatorFactory.eINSTANCE.createEnvironment();
          environment.setRuntime(Runtime.get(runtime));
          nodeCandidates.add(nodeCandidateFactory.of(
              cloud, location, hardware, 0, 0, environment));
        }
      }
    }
    return nodeCandidates;
  }

  static class PriceCache {

    private Map<CloudiatorModel, Map<PriceKey, Double>> cache = new HashMap<>();

    private boolean exists(CloudiatorModel cloudiatorModel) {
      return cache.containsKey(cloudiatorModel);
    }

    private void load(CloudiatorModel cloudiatorModel) {
      Map<PriceKey, Double> prices = new HashMap<>();
      for (Cloud cloud : cloudiatorModel.getClouds()) {
        for (Price price : cloud.getPrices()) {
          prices.put(PriceKey.of(cloud, price), price.getPrice());
        }
      }
      cache.put(cloudiatorModel, prices);
    }

    private Double retrieve(CloudiatorModel cloudiatorModel, PriceKey priceKey) {
      return cache.get(cloudiatorModel).get(priceKey);
    }

    static class PriceKey {

      private final String cloudId;
      private final String imageId;
      private final String hardwareId;
      private final String location;

      private PriceKey(String cloudId, String imageId, String hardwareId, String location) {
        this.cloudId = cloudId;
        this.imageId = imageId;
        this.hardwareId = hardwareId;
        this.location = location;
      }

      static PriceKey of(Cloud cloud, Image image, Hardware hardware, Location location) {
        checkNotNull(cloud, "cloud is null");
        checkNotNull(image, "image is null");
        checkNotNull(hardware, "hardware is null");
        checkNotNull(location, "location is null");
        return new PriceKey(cloud.getId(), image.getId(), hardware.getId(), location.getId());
      }

      static PriceKey of(Cloud cloud, Price price) {
        checkNotNull(cloud, "cloud is null");
        checkNotNull(price, "price is null");
        return PriceKey.of(cloud, price.getImage(), price.getHardware(),
            price.getLocation());
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (!(o instanceof PriceKey)) {
          return false;
        }

        PriceKey priceKey = (PriceKey) o;

        if (!cloudId.equals(priceKey.cloudId)) {
          return false;
        }
        if (!imageId.equals(priceKey.imageId)) {
          return false;
        }
        if (!hardwareId.equals(priceKey.hardwareId)) {
          return false;
        }
        return location.equals(priceKey.location);
      }

      @Override
      public int hashCode() {
        int result = cloudId.hashCode();
        result = 31 * result + imageId.hashCode();
        result = 31 * result + hardwareId.hashCode();
        result = 31 * result + location.hashCode();
        return result;
      }
    }

  }

}
