package org.cloudiator.experiment;

import java.math.BigInteger;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import org.cloudiator.choco.ChocoSolverTesting;
import org.cloudiator.choco.TimeLimit;
import org.cloudiator.experiment.Experiment.CloudiatorModelType;
import org.cloudiator.ocl.NodeCandidate;
import org.cloudiator.ocl.Solution;

public class ExperimentTest {

  public static void main(String[] args) {

    final DoubleSummaryStatistics statisticsSmall = CloudiatorModelType.SMALL
        .getCandidates().stream()
        .filter(nodeCandidate -> nodeCandidate.getHardware().getCores().equals(
            BigInteger.valueOf(2))).mapToDouble(new ToDoubleFunction<NodeCandidate>() {
          @Override
          public double applyAsDouble(NodeCandidate nodeCandidate) {
            return nodeCandidate.getPrice();
          }
        }).summaryStatistics();

    final DoubleSummaryStatistics statisticsHarmony = CloudiatorModelType.CLOUD_HARMONY
        .getCandidates().stream()
        .filter(nodeCandidate -> nodeCandidate.getHardware().getCores().equals(
            BigInteger.valueOf(2))).mapToDouble(new ToDoubleFunction<NodeCandidate>() {
          @Override
          public double applyAsDouble(NodeCandidate nodeCandidate) {
            return nodeCandidate.getPrice();
          }
        }).filter(v -> v != Double.MAX_VALUE).summaryStatistics();

    System.out.println(statisticsSmall.getMin());
    System.out.println(statisticsSmall.getMax());

    System.out.println(statisticsHarmony.getMin());
    System.out.println(statisticsHarmony.getMax());

    System.out.println(CloudiatorModelType.CLOUD_HARMONY.getCandidates().stream()
        .filter(nodeCandidate -> nodeCandidate.getHardware().getCores().equals(
            BigInteger.valueOf(2)))
        .filter(nodeCandidate -> nodeCandidate.getPrice() < Double.MAX_VALUE)
        .filter(nodeCandidate -> nodeCandidate.getPrice() > 40).findAny().get());

    Map<String, Integer> countryCountsSmall = new HashMap<>();
    Map<String, Integer> countryCountsCloudHarmony = new HashMap<>();

    for (NodeCandidate nodeCandidate : CloudiatorModelType.SMALL.getCandidates()) {
      String country = nodeCandidate.getLocation().getGeoLocation().getCountry();
      if (countryCountsSmall.containsKey(country)) {
        countryCountsSmall.put(country, countryCountsSmall.get(country) + 1);
      } else {
        countryCountsSmall.put(country, 1);
      }
    }

    for (NodeCandidate nodeCandidate : CloudiatorModelType.CLOUD_HARMONY.getCandidates()) {
      String country = nodeCandidate.getLocation().getGeoLocation().getCountry();
      if (countryCountsCloudHarmony.containsKey(country)) {
        countryCountsCloudHarmony.put(country, countryCountsCloudHarmony.get(country) + 1);
      } else {
        countryCountsCloudHarmony.put(country, 1);
      }
    }

    System.out.println(countryCountsSmall);
    System.out.println(countryCountsCloudHarmony);

    ChocoSolverTesting chocoSolverTesting = new ChocoSolverTesting(CloudiatorModelType.SMALL.getCandidates());

    final Solution solution5 = chocoSolverTesting.solveDirect(5, new TimeLimit(TimeUnit.MINUTES, 10));
    final Solution solution6 = chocoSolverTesting.solveDirect(6, new TimeLimit(TimeUnit.MINUTES, 10));

    System.out.println(solution5);
    System.out.println(solution6);

    //final Solution solution1 = chocoSolver.solveDirect(8, new TimeLimit(TimeUnit.DAYS, 1));

    //System.out.println(solution1);

  }

}
