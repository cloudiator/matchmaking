package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import com.google.inject.Inject;
import org.cloudiator.messaging.services.HardwareService;

public class HardwareSupplierFactory {

  private final HardwareService hardwareService;

  @Inject
  public HardwareSupplierFactory(HardwareService hardwareService) {
    this.hardwareService = hardwareService;
  }

  public HardwareSupplier newInstance(Cloud cloud, String userId) {
    return new HardwareSupplier(hardwareService, cloud, userId);
  }

}
