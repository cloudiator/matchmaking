package org.cloudiator.matchmaking.experiment;

import cloudiator.Cloud;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import java.math.BigDecimal;
import org.cloudiator.matchmaking.experiment.LargeModelGenerator.LocationGenerator;
import org.cloudiator.matchmaking.ocl.HardwareBasedPriceFunction;
import org.cloudiator.matchmaking.ocl.PriceFunction;

public class ExperimentModelPriceFunction implements PriceFunction {

  private static final HardwareBasedPriceFunction HARDWARE_BASED_PRICE_FUNCTION = new HardwareBasedPriceFunction();

  @Override
  public double calculatePricing(Cloud cloud, Hardware hardware, Location location, Image image,
      String userId) {
    //basic price
    double basicPrice = HARDWARE_BASED_PRICE_FUNCTION
        .calculatePricing(cloud, hardware, location, image, null);

    //include image
    //do nothing at the moment
    int imageCost = 0;

    //include cloud factor
    double cloudFactor = BigDecimal.valueOf(Double.valueOf(cloud.getId())).movePointLeft(1)
        .add(BigDecimal.ONE)
        .doubleValue();

    //include location factor
    double locationFactor = LocationGenerator.OPTIONS.get(location.getGeoLocation().getCountry());

    return (basicPrice + imageCost) * cloudFactor * locationFactor;
  }

  @Override
  public int getPriority() {
    return Priority.LOW;
  }
}
