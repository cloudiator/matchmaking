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
  private final static boolean ENABLED = true;

  public DifferentNodeGenerator(NodeGenerator delegate,
      ConstraintSatisfactionProblem constraintSatisfactionProblem) {
    this.nodeGenerator = delegate;
    this.constraintSatisfactionProblem = constraintSatisfactionProblem;
  }

  @Override
  public Set<NodeCandidate> getPossibleNodes() {

    if (!ENABLED) {
      return nodeGenerator.getPossibleNodes();
    } else {

      Set<NodeCandidate> candidates = nodeGenerator.getPossibleNodes();
      Set<NodeCandidate> cheapestCandidates = new HashSet<>();

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
          cheapestCandidates.add(one);
        }
      }
      System.out
          .println(
              String.format("%s generated %s different nodes", this, cheapestCandidates.size()));
      return cheapestCandidates;
    }
  }

  private boolean effectivelyEqual(NodeCandidate one, NodeCandidate two) {
    return equalWithRespectToHardware(one.getHardware(), two.getHardware())
        && equalWithRespectToImage(one.getImage(), two.getImage())
        && equalWithRespectToLocation(one.getLocation(), two.getLocation())
        && equalWithRespectToCloud(one.getCloud(), two.getCloud());
  }

  private boolean equalWithRespectToImage(Image one, Image two) {
    return one.getOperatingSystem().getFamily().equals(two.getOperatingSystem().getFamily());
  }

  private boolean equalWithRespectToHardware(Hardware one, Hardware two) {
    return one.getCores().equals(two.getCores()) && one.getRam().equals(two.getRam());
  }

  private boolean equalWithRespectToLocation(Location one, Location two) {
    return one.getCountry().equals(two.getCountry());
  }

  private boolean equalWithRespectToCloud(Cloud one, Cloud two) {
    return true;
  }

}
