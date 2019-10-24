package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.Price;
import javax.inject.Inject;

public class PriceModelGenerator {

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private final PriceFunction priceFunction;

  @Inject
  public PriceModelGenerator(PriceFunction priceFunction) {
    this.priceFunction = priceFunction;
  }

  public void generatePriceModel(Cloud cloud, String userId) {

    for (Hardware hardware : cloud.getHardwareList()) {
      for (Image image : cloud.getImages()) {
        for (Location location : cloud.getLocations()) {
          Price price = newPrice();
          price.setHardware(hardware);
          price.setLocation(location);
          price.setImage(image);
          price.setPrice(priceFunction.calculatePricing(cloud, hardware, location, image, userId));
          cloud.getPrices().add(price);
        }
      }
    }
  }

  private Price newPrice() {
    return CLOUDIATOR_FACTORY.createPrice();
  }


}
