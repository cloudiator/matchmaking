package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.CloudType;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import java.util.Optional;

public class HardwareBasedPriceFunction implements PriceFunction {

  @Override
  public Optional<Double> calculatePricing(Cloud cloud, Hardware hardware, Location location, Image image,
      String userId) {

    double price = 0;
    price += hardware.getCores();
    price += hardware.getRam() / 1000;

    if (hardware.getDisk() != null) {
      price += hardware.getDisk() / 100;
    }

    if (cloud.getType().equals(CloudType.PRIVATE)) {
      return Optional.of(price / 2);
    }

    return Optional.of(price);
  }

  @Override
  public int getPriority() {
    return Priority.LOW;
  }
}
