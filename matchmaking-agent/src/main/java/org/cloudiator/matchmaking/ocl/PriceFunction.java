package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import de.uniulm.omi.cloudiator.util.execution.Prioritized;
import java.util.Optional;

public interface PriceFunction extends Prioritized {

  Optional<Double> calculatePricing(Cloud cloud, Hardware hardware, Location location, Image image, String userId);

}
