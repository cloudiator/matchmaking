package experiment;

import choco.TimeLimit;
import cloudiator.CloudiatorModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import org.cloudiator.ocl.Solution;

public class Experiment {

  public enum CloudiatorModelType {
    EXPERIMENT(new ExperimentModelGenerator().generateModel("test")),
    CLOUD_HARMONY(new FileCachedModelGenerator(new CloudHarmony()).generateModel("test"));

    private final CloudiatorModel cloudiatorModel;

    CloudiatorModelType(CloudiatorModel cloudiatorModel) {
      this.cloudiatorModel = cloudiatorModel;
    }

    public CloudiatorModel getCloudiatorModel() {
      return cloudiatorModel;
    }
  }

  private final TimeLimit limit;
  private final int nodeSize;
  private final int repetitions;
  private volatile boolean finished = false;
  private final boolean iterative;
  private final CloudiatorModelType cloudiatorModelType;

  public boolean isIterative() {
    return iterative;
  }

  public TimeLimit getLimit() {
    return limit;
  }

  public int getNodeSize() {
    return nodeSize;
  }

  public int getRepetitions() {
    return repetitions;
  }

  DoubleSummaryStatistics costStatistics;
  DoubleSummaryStatistics timeStatistics;

  private List<Solution> solutions = new ArrayList<>();

  public Experiment(TimeLimit limit, int nodeSize, int repetitions, boolean iterative,
      CloudiatorModelType cloudiatorModelType) {
    this.limit = limit;
    this.nodeSize = nodeSize;
    this.repetitions = repetitions;
    this.iterative = iterative;
    this.cloudiatorModelType = cloudiatorModelType;
  }

  public Optional<Solution> optimal() {
    return solutions.stream().filter(Solution::isOptimal).findAny();
  }


  public void addSolution(Solution solution) {
    if (finished) {
      throw new IllegalStateException();
    }
    this.solutions.add(solution);
  }

  public CloudiatorModelType getCloudiatorModelType() {
    return cloudiatorModelType;
  }

  private void generateStatistics() {
    if (!finished) {
      this.finished = true;
      costStatistics = solutions.stream().mapToDouble(s -> s.getCosts()).summaryStatistics();
      timeStatistics = solutions.stream()
          .mapToDouble(s -> BigDecimal.valueOf(s.getTime()).floatValue()).summaryStatistics();
    }
  }

  public DoubleSummaryStatistics getCostStatistics() {
    generateStatistics();
    return costStatistics;
  }

  public DoubleSummaryStatistics timeStatistics() {
    generateStatistics();
    return timeStatistics;
  }

  @Override
  public String toString() {
    return "Experiment{" +
        "limit=" + limit +
        ", nodeSize=" + nodeSize +
        ", repetitions=" + repetitions +
        ", iterative=" + iterative +
        ", cloudiatorModelType=" + cloudiatorModelType +
        '}';
  }

  public void print() {

    System.out.println(this);

    System.out.println("Average Costs: " + getCostStatistics().getAverage());
    System.out.println("Min Costs: " + getCostStatistics().getMin());
    System.out.println("Max Costs: " + getCostStatistics().getMax());

    System.out.println("Average Time: " + timeStatistics().getAverage());
    System.out.println("Min Time: " + timeStatistics().getMin());
    System.out.println("Max Time: " + timeStatistics().getMax());
  }

}
