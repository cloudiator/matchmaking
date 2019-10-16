package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.DiscoveryItemState;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.LocationScope;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Set;
import org.cloudiator.matchmaking.converters.GeoLocationConverter;
import org.cloudiator.matchmaking.converters.OperatingSystemConverter;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.messages.Byon.ByonNode;

@Singleton
public class ByonGenerator {

  public static final Cloud BYON_CLOUD;
  private static final OperatingSystemConverter OS_CONVERTER = new OperatingSystemConverter();
  private static final GeoLocationConverter GEO_LOCATION_CONVERTER = new GeoLocationConverter();

  static {
    final Cloud cloud = CloudiatorFactory.eINSTANCE.createCloud();
    cloud.setId(ByonCloudUtil.getId());
    cloud.setCloudcredential(ByonCloudUtil.getByonCredential());
    cloud.setConfiguration(ByonCloudUtil.getByonCloudConfiguration());
    cloud.setApi(ByonCloudUtil.getByonApi());
    cloud.setOwner(ByonCloudUtil.getByonOwner());
    BYON_CLOUD = cloud;
  }

  private synchronized Image generateImage(ByonNode byonNode) {

    Image image = CloudiatorFactory.eINSTANCE.createImage();
    image.setProviderId(
        String.format("%s%s", ByonCloudUtil.ByonImage.PROV_ID_PREF, byonNode.getId()));
    final int osFamilyVal = byonNode.getNodeData().getProperties().getOperationSystem()
        .getOperatingSystemFamilyValue();
    final int osVersionVal = byonNode.getNodeData().getProperties().getOperationSystem()
        .getOperatingSystemVersion().getVersion();
    final int osArchVal = byonNode.getNodeData().getProperties().getOperationSystem()
        .getOperatingSystemArchitectureValue();
    image.setName(String
        .format("%s%s_%s_%s", ByonCloudUtil.ByonImage.NAME_PREF, osFamilyVal, osVersionVal,
            osArchVal));
    image.setId(String.format("%s%s", ByonCloudUtil.ByonImage.ID_PREF, byonNode.getId()));
    image.setOperatingSystem(
        OS_CONVERTER.apply(byonNode.getNodeData().getProperties().getOperationSystem()));
    image.setOwner(byonNode.getUserId());
    image.setState(DiscoveryItemState.OK);
    return image;
  }

  private synchronized Hardware generateHardware(ByonNode byonNode) {

    Hardware hardware = CloudiatorFactory.eINSTANCE.createHardware();
    hardware.setProviderId(
        String.format("%s%s", ByonCloudUtil.ByonHardware.PROV_ID_PREF, byonNode.getId()));
    hardware.setId(String.format("%s%s", ByonCloudUtil.ByonHardware.ID_PREF, byonNode.getId()));
    hardware.setName(String
        .format("%s%s", ByonCloudUtil.ByonHardware.NAME_PREF, byonNode.getNodeData().getName()));
    hardware.setCores(byonNode.getNodeData().getProperties().getNumberOfCores());
    hardware.setRam(Math.toIntExact(byonNode.getNodeData().getProperties().getMemory()));
    hardware.setDisk(byonNode.getNodeData().getProperties().getDisk());
    hardware.setOwner(byonNode.getUserId());
    hardware.setState(DiscoveryItemState.OK);

    return hardware;
  }

  private synchronized Location generateLocation(ByonNode byonNode) {

    Location location = CloudiatorFactory.eINSTANCE.createLocation();
    location.setName(String.format("%s", ByonCloudUtil.ByonLocation.NAME_PREF));
    location.setProviderId(
        String.format("%s%s", ByonCloudUtil.ByonLocation.PROV_ID_PREF, byonNode.getId()));
    location.setId(String.format("%s%s", ByonCloudUtil.ByonLocation.ID_PREF, byonNode.getId()));
    location.setGeoLocation(GEO_LOCATION_CONVERTER.apply(byonNode.getNodeData().
        getProperties().getGeoLocation()));
    location.setParent(null);
    location.isAssignable();
    location.setLocationScope(LocationScope.HOST);
    location.setOwner(byonNode.getUserId());
    location.setState(DiscoveryItemState.OK);

    return location;

  }


  public synchronized NodeCandidates getNodeCandidates(Set<ByonNode> byonNodes) {

    Set<NodeCandidate> nodeCandidates = new HashSet<>();

    BYON_CLOUD.getLocations().clear();
    BYON_CLOUD.getImages().clear();
    BYON_CLOUD.getHardwareList().clear();

    for (ByonNode byonNode : byonNodes) {

      final Image image = generateImage(byonNode);
      final Hardware hardware = generateHardware(byonNode);
      final Location location = generateLocation(byonNode);

      BYON_CLOUD.getImages().add(image);
      BYON_CLOUD.getLocations().add(location);
      BYON_CLOUD.getHardwareList().add(hardware);

      nodeCandidates
          .add(NodeCandidateFactory.create().byon(byonNode.getId(), hardware, image, location));

    }

    return NodeCandidates.of(nodeCandidates);
  }
}
