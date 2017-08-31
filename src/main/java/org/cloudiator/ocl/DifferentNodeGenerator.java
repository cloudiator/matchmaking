package org.cloudiator.ocl;

import cloudiator.Cloud;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import java.util.HashSet;
import java.util.Set;

public class DifferentNodeGenerator implements NodeGenerator {

  private final NodeGenerator nodeGenerator;
  private final ConstraintSatisfactionProblem constraintSatisfactionProblem;

  public DifferentNodeGenerator(NodeGenerator delegate,
      ConstraintSatisfactionProblem constraintSatisfactionProblem) {
    this.nodeGenerator = delegate;
    this.constraintSatisfactionProblem = constraintSatisfactionProblem;
  }

  @Override
  public Set<NodeCandidate> getPossibleNodes() {

    Set<NodeCandidate> candidates = nodeGenerator.getPossibleNodes();
    Set<NodeCandidate> cheapestCanidates = new HashSet<>();

    for (NodeCandidate one : new HashSet<>(candidates)) {
      boolean cheapest = true;
      for (NodeCandidate two : candidates) {
        if (effectivelyEqual(one, two)) {
          if (one.getPrice() > two.getPrice()) {
            cheapest = false;
          }
        }
      }
      if (!cheapest) {
        candidates.remove(one);
      }
      if (cheapest) {
        cheapestCanidates.add(one);
      }
    }
    System.out
        .println(String.format("%s generated %s different nodes", this, cheapestCanidates.size()));
    return cheapestCanidates;
  }

  private boolean effectivelyEqual(NodeCandidate one, NodeCandidate two) {
    return equalWithRespectToHardware(one.getHardware(), two.getHardware())
        && equalWithRespectToImage(one.getImage(), two.getImage())
        && equalWithRespectToLocation(one.getLocation(), two.getLocation())
        && equalWithRespectToCloud(one.getCloud(), two.getCloud());
  }

  private boolean equalWithRespectToImage(Image one, Image two) {
    return false;
  }

  private boolean equalWithRespectToHardware(Hardware one, Hardware two) {
    return false;
  }

  private boolean equalWithRespectToLocation(Location one, Location two) {
    return false;
  }

  private boolean equalWithRespectToCloud(Cloud one, Cloud two) {
    return false;
  }

}
