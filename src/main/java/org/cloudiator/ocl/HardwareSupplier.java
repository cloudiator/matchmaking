package org.cloudiator.ocl;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import cloudiator.Hardware;
import cloudiator.Location;
import com.google.common.collect.MoreCollectors;
import java.math.BigInteger;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.cloudiator.messages.Hardware.HardwareQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.HardwareService;

public class HardwareSupplier implements Supplier<Set<Hardware>> {

  private final Cloud cloud;
  private final String userId;
  private final CloudiatorFactory cloudiatorFactory = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private final HardwareService hardwareService;

  public HardwareSupplier(HardwareService hardwareService, Cloud cloud, String userId) {
    this.hardwareService = hardwareService;
    this.cloud = cloud;
    this.userId = userId;
  }

  private HardwareQueryRequest buildRequest() {
    return HardwareQueryRequest.newBuilder().setCloudId(cloud.getId()).setUserId(userId).build();
  }

  @Override
  public Set<Hardware> get() {

    try {
      return hardwareService.getHardware(buildRequest()).getHardwareFlavorsList().stream().map(
          hardwareFlavor -> {
            Hardware hardware = cloudiatorFactory.createHardware();
            hardware.setCores(BigInteger.valueOf(hardwareFlavor.getCores()));
            hardware.setRam(BigInteger.valueOf(hardwareFlavor.getRam()));
            hardware.setDisk(hardwareFlavor.getDisk());
            hardware.setId(hardwareFlavor.getId());
            hardware.setName(hardwareFlavor.getName());
            hardware.setProviderId(hardwareFlavor.getProviderId());

            if (hardwareFlavor.hasLocation()) {
              hardware.setLocation(getLocation(hardwareFlavor.getLocation().getId()));
            }

            return hardware;
          }).collect(Collectors.toSet());
    } catch (ResponseException e) {
      throw new IllegalStateException(String
          .format("Could not retrieve hardware due to communication error: %s", e.getMessage()), e);
    }
  }

  private Location getLocation(String id) {
    return cloud.getLocations().stream()
        .filter(search -> search.getId().equals(id)).collect(MoreCollectors.onlyElement());
  }
}
