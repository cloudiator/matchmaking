package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.CloudType;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;

public class HardwareBasedPriceFunction implements PriceFunction {

  @Override
  public double calculatePricing(Cloud cloud, Hardware hardware, Location location, Image image) {

    double price = 0;
    price += hardware.getCores();
    price += hardware.getRam() / 1000;

    if (hardware.getDisk() != null) {
      price += hardware.getDisk() / 100;
    }

    if (cloud.getType().equals(CloudType.PRIVATE)) {
      return price / 2;
    }

    return price;
  }
}
