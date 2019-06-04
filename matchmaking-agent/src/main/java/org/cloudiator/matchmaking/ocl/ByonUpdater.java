package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.OSArchitecture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import cloudiator.GeoLocation;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.converters.GeoLocationConverter;
import org.cloudiator.matchmaking.converters.OperatingSystemConverter;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.messages.Byon.ByonNode;
import org.cloudiator.messages.entities.CommonEntities.OperatingSystem;
import cloudiator.Location;

@Singleton
public class ByonUpdater {

  private static final Cloud BYON_CLOUD;
  private static final OperatingSystemConverter OS_CONVERTER = new OperatingSystemConverter();
  private static final GeoLocationConverter GEO_LOCATION_CONVERTER = new GeoLocationConverter();

  static {
    final Cloud cloud = CloudiatorFactory.eINSTANCE.createCloud();
    BYON_CLOUD = cloud;
  }

  public synchronized void update(ByonNodeCache cache) {
    final Set<ByonNode> byonNodes = cache.readAll();

    /* updateImages(byonNodes); */
    updateHardwares(byonNodes);
    /* updateLocations(byonNodes); */
  }

  private synchronized void updateImages(Set<ByonNode> byonNodes) {
    BYON_CLOUD.getImages().clear();

    for (ByonNode byonNode : byonNodes) {
      /* Image image = CloudiatorFactory.eINSTANCE.createImage();
      //todo: setting is mandatory? image.setProviderId(...);
      final int osFamilyVal = byonNode.getNodeData().getProperties().getOperationSystem().getOperatingSystemFamilyValue();
      final int osVersionVal = byonNode.getNodeData().getProperties().getOperationSystem().getOperatingSystemVersion().getVersion();
      final int osArchVal = byonNode.getNodeData().getProperties().getOperationSystem().getOperatingSystemArchitectureValue();
      image.setName(String.format("BYON_IMAGE_%s_%s_%s", osFamilyVal, osVersionVal, osArchVal));
      image.setId(String.format("BYON_IMAGE_ID_%s", byonNode.getId()));
      //todo: setting is mandatory? image.setLocation(...);
      image.setOperatingSystem(OS_CONVERTER.apply(byonNode.getNodeData().getProperties().getOperationSystem()));
      BYON_CLOUD.getImages().add(image); */
    }
  }

  private synchronized void updateHardwares(Set<ByonNode> byonNodes) {
    BYON_CLOUD.getHardwareList().clear();

    for (ByonNode byonNode : byonNodes) {
      Hardware hardware = CloudiatorFactory.eINSTANCE.createHardware();
      // todo: setting is mandatory? hardware.setProviderId(...);
      hardware.setId(String.format("BYON_HW_ID_%s", byonNode.getId()));
      hardware.setName(String.format("BYON_HW_%s", byonNode.getNodeData().getName()));
      hardware.setCores(byonNode.getNodeData().getProperties().getNumberOfCores());
      hardware.setRam(Math.toIntExact(byonNode.getNodeData().getProperties().getMemory()));
      hardware.setDisk(byonNode.getNodeData().getProperties().getDisk());
      // todo: setting is mandatory? hardware.setLocation(location);
      BYON_CLOUD.getHardwareList().add(hardware);
    }
  }

  private synchronized void updateLocations(Set<ByonNode> byonNodes) {
    BYON_CLOUD.getLocations().clear();

    for (ByonNode byonNode : byonNodes) {
      /* Location location = CloudiatorFactory.eINSTANCE.createLocation();
      //todo: setting is mandatory? location.setName(...);
      //todo: setting is mandatory? location.setProviderId(...);
      //todo: setting is mandatory? location.setId(...)
      location.setGeoLocation(GEO_LOCATION_CONVERTER.apply(byonNode.getNodeData().
          getProperties().getGeoLocation()));
      BYON_CLOUD.getLocations().add(location);*/
    }
  }
}