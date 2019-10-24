package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import com.google.inject.Inject;
import de.uniulm.omi.cloudiator.util.execution.Prioritized;
import java.util.Set;
import java.util.TreeSet;

public class CompositePriceFunction implements PriceFunction {

  private final Set<PriceFunction> priceFunctions;

  @Inject
  public CompositePriceFunction(
      Set<PriceFunction> priceFunctions) {
    this.priceFunctions = new TreeSet<>(Prioritized::compareTo);
    this.priceFunctions.addAll(priceFunctions);
  }

  @Override
  public double calculatePricing(Cloud cloud, Hardware hardware, Location location, Image image,
      String userId) {

    for (PriceFunction priceFunction : priceFunctions) {
      try {
        return priceFunction.calculatePricing(cloud, hardware, location, image, userId);
      } catch (Exception e) {
        //ignore
      }
    }

    throw new IllegalStateException("Could not calculate pricing.");
  }

  @Override
  public int getPriority() {
    return Priority.HIGH;
  }
}
