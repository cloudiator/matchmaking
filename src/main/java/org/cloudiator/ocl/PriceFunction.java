package org.cloudiator.ocl;

import cloudiator.Cloud;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;

public interface PriceFunction {

  double calculatePricing(Cloud cloud, Hardware hardware, Location location, Image image);

}
