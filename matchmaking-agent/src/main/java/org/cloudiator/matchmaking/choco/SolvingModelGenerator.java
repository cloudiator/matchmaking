package org.cloudiator.matchmaking.choco;

import cloudiator.Api;
import cloudiator.Cloud;
import cloudiator.CloudConfiguration;
import cloudiator.CloudCredential;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.GeoLocation;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.Node;
import cloudiator.OSArchitecture;
import cloudiator.OSFamily;
import cloudiator.OperatingSystem;
import cloudiator.Price;
import cloudiator.Property;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.ocl.NodeCandidates;


public class SolvingModelGenerator implements Function<NodeCandidates, CloudiatorModel> {

  private static class SolvingModelGeneratorInternal {

    private final NodeCandidates nodeCandidates;
    private final CloudiatorModel model;
    private final Map<String, Cloud> cloudMap = new HashMap<>();
    private final Map<String, Location> locationMap = new HashMap<>();
    private final Map<String, Hardware> hardwareMap = new HashMap<>();
    private final Map<String, Image> imageMap = new HashMap<>();
    private final Map<OsId, OperatingSystem> osMap = new HashMap<>();


    private SolvingModelGeneratorInternal(NodeCandidates nodeCandidates) {
      this.nodeCandidates = nodeCandidates;
      this.model = CloudiatorFactory.eINSTANCE.createCloudiatorModel();
    }

    private CloudiatorModel generate() {
      importNodeCandidates();
      return model;
    }

    private Cloud handleCloud(Cloud cloud) {
      Cloud modelCloud = cloudMap.get(cloud.getId());
      if (modelCloud == null) {
        Cloud newCloud = CloudiatorFactory.eINSTANCE.createCloud();
        Api newApi = CloudiatorFactory.eINSTANCE.createApi();
        newApi.setProviderName(cloud.getApi().getProviderName());
        CloudCredential cloudCredential = CloudiatorFactory.eINSTANCE.createCloudCredential();
        cloudCredential.setSecret(cloud.getCloudcredential().getSecret());
        cloudCredential.setUser(cloud.getCloudcredential().getUser());
        CloudConfiguration newCloudConfiguration = CloudiatorFactory.eINSTANCE
            .createCloudConfiguration();
        newCloudConfiguration
            .setNodeGroup(cloud.getConfiguration().getNodeGroup());
        for (Property property : cloud.getConfiguration().getProperties()) {
          Property newProperty = CloudiatorFactory.eINSTANCE.createProperty();
          newProperty.setKey(property.getKey());
          newProperty.setValue(property.getValue());
          newCloudConfiguration.getProperties().add(newProperty);
        }
        newCloud.setApi(newApi);
        newCloud.setCloudcredential(cloudCredential);
        newCloud.setConfiguration(newCloudConfiguration);
        newCloud.setEndpoint(cloud.getEndpoint());
        newCloud.setId(cloud.getId());
        newCloud.setType(cloud.getType());
        newCloud.setDiagnostic(cloud.getDiagnostic());
        newCloud.setState(cloud.getState());
        newCloud.setOwner(cloud.getOwner());
        cloudMap.put(newCloud.getId(), newCloud);
        model.getClouds().add(newCloud);
        modelCloud = newCloud;
      }
      return modelCloud;
    }

    private Hardware handleHardware(Cloud cloud, Hardware hardware) {
      Hardware modelHardware = hardwareMap.get(hardware.getId());
      if (modelHardware == null) {
        Hardware newHardware = CloudiatorFactory.eINSTANCE.createHardware();
        newHardware.setId(hardware.getId());
        newHardware.setProviderId(hardware.getProviderId());
        newHardware.setName(hardware.getName());
        newHardware.setCores(hardware.getCores());
        newHardware.setRam(hardware.getRam());
        newHardware.setDisk(hardware.getDisk());
        newHardware.setState(hardware.getState());
        newHardware.setOwner(hardware.getOwner());
        newHardware.setLocation(handleLocation(cloud, hardware.getLocation()));
        hardwareMap.put(newHardware.getId(), newHardware);
        modelHardware = newHardware;
        cloud.getHardwareList().add(hardware);
      }
      return modelHardware;
    }

    private Location handleLocation(Cloud cloud, @Nullable Location location) {
      if (location == null) {
        return null;
      }
      Location modelLocation = locationMap.get(location.getId());
      if (modelLocation == null) {
        Location newLocation = CloudiatorFactory.eINSTANCE.createLocation();
        newLocation.setId(location.getId());
        newLocation.setName(location.getName());
        newLocation.setProviderId(location.getProviderId());
        newLocation.setParent(handleLocation(cloud, location.getParent()));
        if (location.getGeoLocation() != null) {
          GeoLocation newGeoLocation = CloudiatorFactory.eINSTANCE.createGeoLocation();
          newGeoLocation.setCity(location.getGeoLocation().getCity());
          newGeoLocation.setCountry(location.getGeoLocation().getCountry());
          newGeoLocation.setLatitude(location.getGeoLocation().getLatitude());
          newGeoLocation.setLongitude(location.getGeoLocation().getLatitude());
          newLocation.setGeoLocation(newGeoLocation);
        }

        newLocation.setAssignable(location.isAssignable());
        newLocation.setLocationScope(location.getLocationScope());
        newLocation.setOwner(location.getOwner());
        newLocation.setState(location.getState());
        locationMap.put(newLocation.getId(), newLocation);
        cloud.getLocations().add(location);
        modelLocation = newLocation;
      }
      return modelLocation;
    }

    private Image handleImage(Cloud cloud, Image image) {
      Image modelImage = imageMap.get(image.getId());
      if (modelImage == null) {
        Image newImage = CloudiatorFactory.eINSTANCE.createImage();
        newImage.setId(image.getId());
        newImage.setProviderId(image.getProviderId());
        newImage.setName(image.getName());
        newImage.setLocation(handleLocation(cloud, image.getLocation()));
        newImage.setOwner(image.getOwner());
        newImage.setOperatingSystem(handleOs(image.getOperatingSystem().getFamily(),
            image.getOperatingSystem().getVersion(), image.getOperatingSystem().getArchitecture()));
        newImage.setState(image.getState());
        imageMap.put(newImage.getId(), newImage);
        cloud.getImages().add(newImage);
        modelImage = newImage;
      }
      return modelImage;
    }

    private OperatingSystem handleOs(OSFamily family, Integer version, OSArchitecture arch) {
      OsId id = new OsId(family, arch, version);
      OperatingSystem modelOs = osMap.get(id);
      if (modelOs == null) {
        final OperatingSystem newOperatingSystem = CloudiatorFactory.eINSTANCE
            .createOperatingSystem();
        newOperatingSystem.setFamily(family);
        newOperatingSystem.setVersion(version);
        newOperatingSystem.setArchitecture(arch);
        model.getOperatingsystems().add(newOperatingSystem);
        osMap.put(id, newOperatingSystem);
        modelOs = newOperatingSystem;
      }
      return modelOs;
    }

    private void importNodeCandidates() {

      for (NodeCandidate nodeCandidate : nodeCandidates) {

        //handle the cloud
        final Cloud cloud = handleCloud(nodeCandidate.getCloud());

        //handle the location
        final Location location = handleLocation(cloud, nodeCandidate.getLocation());

        //handle the hardware
        final Hardware hardware = handleHardware(cloud, nodeCandidate.getHardware());

        //handle the image
        final Image image = handleImage(cloud, nodeCandidate.getImage());

        //handle the price
        final Price price = CloudiatorFactory.eINSTANCE.createPrice();
        price.setHardware(hardware);
        price.setImage(image);
        price.setLocation(location);
        price.setPrice(nodeCandidate.getPrice());
        cloud.getPrices().add(price);

        //create a new node
        final Node node = CloudiatorFactory.eINSTANCE.createNode();
        node.setId(nodeCandidate.id());
        node.setCloud(cloud);
        node.setImage(image);
        node.setHardware(hardware);
        node.setLocation(location);
        node.setEnvironment(nodeCandidate.getEnvironment());
        node.setMemoryPrice(nodeCandidate.getMemoryPrice());
        node.setPricePerInvocation(nodeCandidate.getPricePerInvocation());
        node.setType(nodeCandidate.getType());
        node.setPrice(price.getPrice());
        model.getNodes().add(node);
      }
    }

  }

  @Override
  public CloudiatorModel apply(NodeCandidates nodeCandidates) {

    return new SolvingModelGeneratorInternal(nodeCandidates).generate();
  }

  private static class OsId {

    private final OSFamily osFamily;
    private final OSArchitecture osArchitecture;
    @Nullable
    private final Integer version;

    private OsId(OSFamily osFamily, OSArchitecture osArchitecture, @Nullable Integer version) {
      this.osFamily = osFamily;
      this.osArchitecture = osArchitecture;
      this.version = version;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      OsId osId = (OsId) o;
      return osFamily == osId.osFamily &&
          osArchitecture == osId.osArchitecture &&
          Objects.equals(version, osId.version);
    }

    @Override
    public int hashCode() {
      return Objects.hash(osFamily, osArchitecture, version);
    }
  }
}
