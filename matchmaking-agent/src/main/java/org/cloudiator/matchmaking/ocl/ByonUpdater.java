package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.Hardware;
import cloudiator.Image;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import org.cloudiator.matchmaking.converters.GeoLocationConverter;
import org.cloudiator.matchmaking.converters.OperatingSystemConverter;
import org.cloudiator.messages.Byon.ByonNode;
import cloudiator.Location;

@Singleton
public class ByonUpdater {

  public static final Cloud BYON_CLOUD;
  private static final OperatingSystemConverter OS_CONVERTER = new OperatingSystemConverter();
  private static final GeoLocationConverter GEO_LOCATION_CONVERTER = new GeoLocationConverter();

  static {
    final Cloud cloud = CloudiatorFactory.eINSTANCE.createCloud();
    cloud.setId(ByonCloudUtil.getId());
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
      image.setProviderId(String.format("BYON_IMAGE_PROV_ID_%s", byonNode.getId()));
      final int osFamilyVal = byonNode.getNodeData().getProperties().getOperationSystem().getOperatingSystemFamilyValue();
      final int osVersionVal = byonNode.getNodeData().getProperties().getOperationSystem().getOperatingSystemVersion().getVersion();
      final int osArchVal = byonNode.getNodeData().getProperties().getOperationSystem().getOperatingSystemArchitectureValue();
      image.setName(String.format("BYON_IMAGE_%s_%s_%s", osFamilyVal, osVersionVal, osArchVal));
      image.setId(String.format("BYON_IMAGE_ID_%s", byonNode.getId()));
      image.setLocation(buildLocation(byonNode));
      image.setOperatingSystem(OS_CONVERTER.apply(byonNode.getNodeData().getProperties().getOperationSystem()));
      BYON_CLOUD.getImages().add(image);
    }
  }

  private synchronized void updateHardwares(Set<ByonNode> byonNodes) {
    BYON_CLOUD.getHardwareList().clear();

    for (ByonNode byonNode : byonNodes) {
      Hardware hardware = CloudiatorFactory.eINSTANCE.createHardware();
      hardware.setProviderId(String.format("BYON_HW_PROV_ID_%s", byonNode.getId()));
      hardware.setId(String.format("BYON_HW_ID_%s", byonNode.getId()));
      hardware.setName(String.format("BYON_HW_%s", byonNode.getNodeData().getName()));
      hardware.setCores(byonNode.getNodeData().getProperties().getNumberOfCores());
      hardware.setRam(Math.toIntExact(byonNode.getNodeData().getProperties().getMemory()));
      hardware.setDisk(byonNode.getNodeData().getProperties().getDisk());
      hardware.setLocation(buildLocation(byonNode));
      BYON_CLOUD.getHardwareList().add(hardware);
    }
  }

  private synchronized void updateLocations(Set<ByonNode> byonNodes) {
    BYON_CLOUD.getLocations().clear();

    for (ByonNode byonNode : byonNodes) {
      Location location = buildLocation(byonNode);
      BYON_CLOUD.getLocations().add(location);
    }
  }

  private synchronized Location buildLocation(ByonNode byonNode) {
    Location location = CloudiatorFactory.eINSTANCE.createLocation();
    //todo: setting is mandatory? location.setName(...);
    //todo: setting is mandatory? location.setProviderId(...);
    location.setId(String.format("BYON_LOCATION_ID_%s", byonNode.getId()));
    location.setGeoLocation(GEO_LOCATION_CONVERTER.apply(byonNode.getNodeData().
        getProperties().getGeoLocation()));
    location.setParent(null);
    return location;
  }

  public synchronized Set<ByonTriple> getValidTriples() {
    Set<ByonTriple> triples = new HashSet<>();
    for (Image image : BYON_CLOUD.getImages()) {
      for (Hardware hardware : BYON_CLOUD.getHardwareList()) {
        for (Location location : BYON_CLOUD.getLocations()) {
          //check if valid combination
          if (isValidByonCombination(image, hardware, location)) {
            triples.add(new ByonTriple(image,hardware,location));
          }
        }
      }
    }

    return triples;
  }

  private synchronized boolean isValidByonCombination(Image image, Hardware hardware, Location location) {
    String[] partsImage = image.getId().split("_");
    String[] partsHardware = hardware.getId().split("_");
    String[] partsLocation = location.getId().split("_");

    if(partsImage.length != 4 || partsHardware.length != 4 || partsLocation.length != 4) {
      return false;
    }

    // valid combo if id postfixes are equal
    final String imageId = partsImage[3];
    final String hardwareId = partsHardware[3];
    final String locationId = partsLocation[3];

    if(imageId.equals(hardwareId) && imageId.equals(locationId)) {
      return true;
    }

    return false;
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