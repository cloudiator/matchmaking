package org.cloudiator.ocl;

import cloudiator.Cloud;
import cloudiator.CloudType;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage;
import org.cloudiator.messages.Cloud.CloudQueryRequest;
import org.cloudiator.messaging.ResponseException;
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

  public void generate() {
    try {
      cloudService.getClouds(buildQuery()).getCloudsList().stream().map(c -> {
        final Cloud cloud = cloudiatorFactory.createCloud();
        cloud.setId(c.getId());


        switch (c.getCloudType()) {
          case PUBLIC_CLOUD:
            cloud.setType(CloudType.PUBLIC);
            break;
          case PRIVATE_CLOUD:
            cloud.setType(CloudType.PRIVATE);
            break;
          case UNRECOGNIZED:
            throw new AssertionError("Unknown cloud type " + c.getCloudType());
        }

        //add locations first as hardware and images need to related to locations
        cloud.getLocations().addAll(locationSupplierFactory.newInstance(cloud, userId).get());
        cloud.getHardwareList().addAll(hardwareSupplierFactory.newInstance(cloud, userId).get());
        cloud.getImages().addAll(imageSupplierFactory.newInstance(cloud, userId).get());

        priceModelGenerator.generatePriceModel(cloud);

        return cloud;
      }).forEach(cloud -> cloudiatorModel.getClouds().add(cloud));
    } catch (ResponseException e) {
      throw new IllegalStateException(
          String.format("Could not retrieve cloud list due to error %s", e.getMessage()), e);
    }
  }

}
