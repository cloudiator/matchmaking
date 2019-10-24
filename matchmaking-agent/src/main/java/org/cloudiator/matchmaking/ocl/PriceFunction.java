package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import de.uniulm.omi.cloudiator.util.execution.Prioritized;

public interface PriceFunction extends Prioritized {

  double calculatePricing(Cloud cloud, Hardware hardware, Location location, Image image, String userId);

}
