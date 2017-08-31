package org.cloudiator.ocl;

import cloudiator.CloudiatorModel;
import com.google.inject.Inject;
import org.cloudiator.messaging.services.CloudService;

public class CloudModelGeneratorFactory {

  private final CloudService cloudService;
  private final ImageSupplierFactory imageSupplierFactory;
  private final HardwareSupplierFactory hardwareSupplierFactory;
  private final LocationSupplierFactory locationSupplierFactory;

  @Inject
  public CloudModelGeneratorFactory(CloudService cloudService,
      ImageSupplierFactory imageSupplierFactory,
      HardwareSupplierFactory hardwareSupplierFactory,
      LocationSupplierFactory locationSupplierFactory) {
    this.cloudService = cloudService;
    this.imageSupplierFactory = imageSupplierFactory;
    this.hardwareSupplierFactory = hardwareSupplierFactory;
    this.locationSupplierFactory = locationSupplierFactory;
  }

  public CloudModelGenerator newInstance(String userId, CloudiatorModel cloudiatorModel) {
    return new CloudModelGenerator(cloudiatorModel, userId, cloudService, hardwareSupplierFactory,
        imageSupplierFactory, locationSupplierFactory);
  }

}
