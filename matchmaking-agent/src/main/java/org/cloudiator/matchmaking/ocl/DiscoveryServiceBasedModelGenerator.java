package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage;
import org.cloudiator.matchmaking.converters.CloudConverter;
import org.cloudiator.messages.Cloud.CloudQueryRequest;
import org.cloudiator.messaging.services.CloudService;

public class DiscoveryServiceBasedModelGenerator {

  private final CloudiatorFactory cloudiatorFactory = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private final CloudiatorModel cloudiatorModel;
  private final String userId;
  private final CloudService cloudService;
  private final HardwareSupplierFactory hardwareSupplierFactory;
  private final ImageSupplierFactory imageSupplierFactory;
  private final LocationSupplierFactory locationSupplierFactory;
  private final PriceModelGenerator priceModelGenerator;
  private final static CloudConverter CLOUD_CONVERTER = new CloudConverter();

  public DiscoveryServiceBasedModelGenerator(CloudiatorModel cloudiatorModel, String userId,
      CloudService cloudService, HardwareSupplierFactory hardwareSupplierFactory,
      ImageSupplierFactory imageSupplierFactory,
      LocationSupplierFactory locationSupplierFactory,
      PriceModelGenerator priceModelGenerator) {
    this.cloudiatorModel = cloudiatorModel;
    this.userId = userId;
    this.cloudService = cloudService;
    this.hardwareSupplierFactory = hardwareSupplierFactory;
    this.imageSupplierFactory = imageSupplierFactory;
    this.locationSupplierFactory = locationSupplierFactory;
    this.priceModelGenerator = priceModelGenerator;
  }

  private CloudQueryRequest buildQuery() {
    return CloudQueryRequest.newBuilder().setUserId(userId).build();
  }

  public void generate() throws ModelGenerationException {
    try {
      cloudService.getClouds(buildQuery()).getCloudsList().stream().map(c -> {

        Cloud cloud = CLOUD_CONVERTER.applyBack(c);

        //add locations first as hardware and images need to related to locations
        cloud.getLocations().addAll(locationSupplierFactory.newInstance(cloud, userId).get());
        cloud.getHardwareList().addAll(hardwareSupplierFactory.newInstance(cloud, userId).get());
        cloud.getImages().addAll(imageSupplierFactory.newInstance(cloud, userId).get());

        priceModelGenerator.generatePriceModel(cloud, userId);

        return cloud;
      }).forEach(cloud -> cloudiatorModel.getClouds().add(cloud));
    } catch (Exception e) {
      throw new ModelGenerationException(
          String.format("Could not generate model due to error %s", e.getMessage()), e);
    }
  }

}
