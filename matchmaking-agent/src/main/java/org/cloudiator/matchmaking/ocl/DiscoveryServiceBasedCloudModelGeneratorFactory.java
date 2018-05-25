package org.cloudiator.matchmaking.ocl;

import cloudiator.CloudiatorModel;
import com.google.inject.Inject;
import org.cloudiator.messaging.services.CloudService;

public class DiscoveryServiceBasedCloudModelGeneratorFactory {

  private final CloudService cloudService;
  private final ImageSupplierFactory imageSupplierFactory;
  private final HardwareSupplierFactory hardwareSupplierFactory;
  private final LocationSupplierFactory locationSupplierFactory;
  private final PriceModelGenerator priceModelGenerator;

  @Inject
  public DiscoveryServiceBasedCloudModelGeneratorFactory(CloudService cloudService,
      ImageSupplierFactory imageSupplierFactory,
      HardwareSupplierFactory hardwareSupplierFactory,
      LocationSupplierFactory locationSupplierFactory,
      PriceModelGenerator priceModelGenerator) {
    this.cloudService = cloudService;
    this.imageSupplierFactory = imageSupplierFactory;
    this.hardwareSupplierFactory = hardwareSupplierFactory;
    this.locationSupplierFactory = locationSupplierFactory;
    this.priceModelGenerator = priceModelGenerator;
  }

  public DiscoveryServiceBasedModelGenerator newInstance(String userId,
      CloudiatorModel cloudiatorModel) {
    return new DiscoveryServiceBasedModelGenerator(cloudiatorModel, userId, cloudService,
        hardwareSupplierFactory,
        imageSupplierFactory, locationSupplierFactory, priceModelGenerator);
  }

}
