package org.cloudiator.ocl;

import cloudiator.Cloud;
import cloudiator.Hardware;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.cloudiator.converters.HardwareConverter;
import org.cloudiator.messages.Hardware.HardwareQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.HardwareService;

public class HardwareSupplier implements Supplier<Set<Hardware>> {

  private static final HardwareConverter HARDWARE_CONVERTER = new HardwareConverter();
  private final Cloud cloud;
  private final String userId;
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
          HARDWARE_CONVERTER).collect(Collectors.toSet());
    } catch (ResponseException e) {
      throw new IllegalStateException(String
          .format("Could not retrieve hardware due to communication error: %s", e.getMessage()), e);
    }
  }

}
