package org.cloudiator.experiment;

import cloudiator.Cloud;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import org.cloudiator.experiment.ExperimentModelGenerator.LocationGenerator;
import java.math.BigDecimal;
import org.cloudiator.ocl.HardwareBasedPriceFunction;
import org.cloudiator.ocl.PriceFunction;

public class ExperimentModelPriceFunction implements PriceFunction {

  private static final HardwareBasedPriceFunction HARDWARE_BASED_PRICE_FUNCTION = new HardwareBasedPriceFunction();

  @Override
  public double calculatePricing(Cloud cloud, Hardware hardware, Location location, Image image) {
    //basic price
    double basicPrice = HARDWARE_BASED_PRICE_FUNCTION
        .calculatePricing(cloud, hardware, location, image);

    //include image
    //do nothing at the moment
    int imageCost = 0;

    //include cloud factor
    double cloudFactor = BigDecimal.valueOf(Double.valueOf(cloud.getId())).movePointLeft(1)
        .add(BigDecimal.ONE)
        .doubleValue();

    //include location factor
    double locationFactor = LocationGenerator.OPTIONS.get(location.getCountry());

    return (basicPrice + imageCost) * cloudFactor * locationFactor;
  }
}
