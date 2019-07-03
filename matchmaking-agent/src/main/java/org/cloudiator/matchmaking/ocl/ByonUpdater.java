package org.cloudiator.matchmaking.ocl;

import cloudiator.Api;
import cloudiator.Cloud;
import cloudiator.CloudConfiguration;
import cloudiator.CloudCredential;
import cloudiator.CloudiatorFactory;
import cloudiator.DiscoveryItemState;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.LocationScope;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import org.cloudiator.matchmaking.converters.GeoLocationConverter;
import org.cloudiator.matchmaking.converters.OperatingSystemConverter;
import org.cloudiator.matchmaking.ocl.ByonCloudUtil.ByonImage;
import org.cloudiator.matchmaking.ocl.ByonCloudUtil.ByonLocation;
import org.cloudiator.messages.Byon.ByonNode;
import cloudiator.Location;

@Singleton
public class ByonUpdater {

  public static final Cloud BYON_CLOUD;
  private static final OperatingSystemConverter OS_CONVERTER = new OperatingSystemConverter();
  private static final GeoLocationConverter GEO_LOCATION_CONVERTER = new GeoLocationConverter();
  private static final Api BYON_API = CloudiatorFactory.eINSTANCE.createApi();
  private static final CloudConfiguration BYON_CC = CloudiatorFactory.eINSTANCE.createCloudConfiguration();
  private static final CloudCredential BYON_CREDENTIAL = CloudiatorFactory.eINSTANCE.createCloudCredential();

  static {

    BYON_API.setProviderName("BYON");
    BYON_CC.setNodeGroup("byon");
    BYON_CREDENTIAL.setUser("byon");
    BYON_CREDENTIAL.setSecret("byon");
    final Cloud cloud = CloudiatorFactory.eINSTANCE.createCloud();
    cloud.setId(ByonCloudUtil.getId());
    cloud.setCloudcredential(BYON_CREDENTIAL);
    cloud.setConfiguration(BYON_CC);
    cloud.setApi(BYON_API);
    BYON_CLOUD = cloud;
  }

  public synchronized void update(ByonNodeCache cache) {
    final Set<ByonNode> byonNodes = cache.readAll();

    updateImages(byonNodes);
    updateHardwares(byonNodes);
    updateLocations(byonNodes);
  }

  private synchronized void updateImages(Set<ByonNode> byonNodes) {
    BYON_CLOUD.getImages().clear();

    for (ByonNode byonNode : byonNodes) {
      Image image = CloudiatorFactory.eINSTANCE.createImage();
      image.setProviderId(String.format("%s%s", ByonCloudUtil.ByonImage.PROV_ID_PREF, byonNode.getId()));
      final int osFamilyVal = byonNode.getNodeData().getProperties().getOperationSystem().getOperatingSystemFamilyValue();
      final int osVersionVal = byonNode.getNodeData().getProperties().getOperationSystem().getOperatingSystemVersion().getVersion();
      final int osArchVal = byonNode.getNodeData().getProperties().getOperationSystem().getOperatingSystemArchitectureValue();
      image.setName(String.format("%s%s_%s_%s", ByonCloudUtil.ByonImage.NAME_PREF, osFamilyVal, osVersionVal, osArchVal));
      image.setId(String.format("%s%s", ByonCloudUtil.ByonImage.ID_PREF, byonNode.getId()));
      image.setOperatingSystem(OS_CONVERTER.apply(byonNode.getNodeData().getProperties().getOperationSystem()));
      image.setOwner(byonNode.getUserId());
      image.setState(DiscoveryItemState.OK);
      BYON_CLOUD.getImages().add(image);
    }
  }

  private synchronized void updateHardwares(Set<ByonNode> byonNodes) {
    BYON_CLOUD.getHardwareList().clear();

    for (ByonNode byonNode : byonNodes) {
      Hardware hardware = CloudiatorFactory.eINSTANCE.createHardware();
      hardware.setProviderId(String.format("%s%s", ByonCloudUtil.ByonHardware.PROV_ID_PREF, byonNode.getId()));
      hardware.setId(String.format("%s%s", ByonCloudUtil.ByonHardware.ID_PREF, byonNode.getId()));
      hardware.setName(String.format("%s%s", ByonCloudUtil.ByonHardware.NAME_PREF, byonNode.getNodeData().getName()));
      hardware.setCores(byonNode.getNodeData().getProperties().getNumberOfCores());
      hardware.setRam(Math.toIntExact(byonNode.getNodeData().getProperties().getMemory()));
      hardware.setDisk(byonNode.getNodeData().getProperties().getDisk());
      hardware.setOwner(byonNode.getUserId());
      hardware.setState(DiscoveryItemState.OK);
      BYON_CLOUD.getHardwareList().add(hardware);
    }
  }

  private synchronized void updateLocations(Set<ByonNode> byonNodes) {
    BYON_CLOUD.getLocations().clear();

    for (ByonNode byonNode : byonNodes) {
      Location location = CloudiatorFactory.eINSTANCE.createLocation();
      location.setName(String.format("%s", ByonCloudUtil.ByonLocation.NAME_PREF));
      location.setProviderId(String.format("%s%s", ByonCloudUtil.ByonLocation.PROV_ID_PREF, byonNode.getId()));
      location.setId(String.format("%s%s", ByonCloudUtil.ByonLocation.ID_PREF, byonNode.getId()));
      location.setGeoLocation(GEO_LOCATION_CONVERTER.apply(byonNode.getNodeData().
          getProperties().getGeoLocation()));
      location.setParent(null);
      location.isAssignable();
      location.setLocationScope(LocationScope.HOST);
      location.setOwner(byonNode.getUserId());
      location.setState(DiscoveryItemState.OK);
      BYON_CLOUD.getLocations().add(location);
    }
  }

  public synchronized Set<ByonTriple> getValidTriples() {
    Set<ByonTriple> triples = new HashSet<>();
    for (Image image : BYON_CLOUD.getImages()) {
      for (Hardware hardware : BYON_CLOUD.getHardwareList()) {
        for (Location location : BYON_CLOUD.getLocations()) {
          //check if valid combination
          if (ByonCloudUtil.isValidByonCombination(image, hardware, location)) {
            triples.add(new ByonTriple(image,hardware,location));
          }
        }
      }
    }

    return triples;
  }


  public static class ByonTriple {
    public final Image image;
    public final Hardware hardware;
    public final Location location;

    private ByonTriple(Image image, Hardware hardware, Location location) {
      this.image = image;
      this.hardware = hardware;
      this.location = location;
    }
  }
}