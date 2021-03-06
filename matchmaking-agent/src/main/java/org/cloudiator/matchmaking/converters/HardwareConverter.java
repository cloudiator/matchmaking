package org.cloudiator.matchmaking.converters;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import cloudiator.Hardware;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.HardwareFlavor;
import org.cloudiator.messages.entities.IaasEntities.HardwareFlavor.Builder;

public class HardwareConverter implements TwoWayConverter<IaasEntities.HardwareFlavor, Hardware> {

  private static final LocationConverter LOCATION_CONVERTER = new LocationConverter();
  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private static final DiscoveryItemStateConverter DISCOVERY_ITEM_STATE_CONVERTER = DiscoveryItemStateConverter.INSTANCE;

  @Override
  public HardwareFlavor applyBack(Hardware hardware) {
    if (hardware == null) {
      return null;
    }

    final Builder builder = HardwareFlavor.newBuilder()
        .setId(hardware.getId())
        .setName(hardware.getName())
        .setProviderId(hardware.getProviderId())
        .setRam(hardware.getRam())
        .setCores(hardware.getCores())
        .setState(DISCOVERY_ITEM_STATE_CONVERTER.apply(hardware.getState()))
        .setUserId(hardware.getOwner());
    if (hardware.getLocation() != null) {
      builder.setLocation(LOCATION_CONVERTER.applyBack(hardware.getLocation()));
    }
    if (hardware.getDisk() != null) {
      builder.setDisk(
          hardware.getDisk());
    }
    return builder.build();
  }

  @Override
  public Hardware apply(HardwareFlavor hardwareFlavor) {
    if (hardwareFlavor == null) {
      return null;
    }
    final Hardware hardwareModel = CLOUDIATOR_FACTORY.createHardware();
    if (hardwareFlavor.hasLocation()) {
      hardwareModel.setLocation(LOCATION_CONVERTER.apply(hardwareFlavor.getLocation()));
    }
    hardwareModel.setProviderId(hardwareFlavor.getProviderId());
    hardwareModel.setName(hardwareFlavor.getName());
    hardwareModel.setId(hardwareFlavor.getId());
    hardwareModel.setCores(hardwareFlavor.getCores());
    hardwareModel.setRam((int) hardwareFlavor.getRam());
    hardwareModel.setDisk(hardwareFlavor.getDisk());
    hardwareModel.setState(DISCOVERY_ITEM_STATE_CONVERTER.applyBack(hardwareFlavor.getState()));
    hardwareModel.setOwner(hardwareFlavor.getUserId());

    return hardwareModel;
  }
}
