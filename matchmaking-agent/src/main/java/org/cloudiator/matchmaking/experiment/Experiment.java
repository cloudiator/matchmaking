package org.cloudiator.matchmaking.experiment;

import cloudiator.CloudiatorModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoublePredicate;
import org.cloudiator.matchmaking.choco.TimeLimit;
import org.cloudiator.matchmaking.ocl.ConsistentNodeGenerator;
import org.cloudiator.matchmaking.ocl.ConstraintChecker;
import org.cloudiator.matchmaking.ocl.DefaultNodeGenerator;
import org.cloudiator.matchmaking.ocl.ModelGenerationException;
import org.cloudiator.matchmaking.ocl.ModelGenerator;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.Solution;

public class Experiment {


  private final TimeLimit limit;
  private final int nodeSize;
  private final int repetitions;
  private final boolean iterative;
  private final CloudiatorModelType cloudiatorModelType;
  DoubleSummaryStatistics costStatistics;
  DoubleSummaryStatistics timeStatistics;
  private volatile boolean finished = false;
  private List<Solution> solutions = new ArrayList<>();

  public Experiment(TimeLimit limit, int nodeSize, int repetitions, boolean iterative,
      CloudiatorModelType cloudiatorModelType) {
    this.limit = limit;
    this.nodeSize = nodeSize;
    this.repetitions = repetitions;
    this.iterative = iterative;
    this.cloudiatorModelType = cloudiatorModelType;
  }

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

  public Optional<Solution> optimal() {
    return solutions.stream().filter(Solution::isOptimal).findAny();
  }

  public boolean hasSolution() {
    return solutions.size() > 0;
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
      costStatistics = solutions.stream().mapToDouble(s -> s.getCosts()).filter(
          new DoublePredicate() {
            @Override
            public boolean test(double v) {
              return v != 0;
            }
          }).summaryStatistics();
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

  public enum CloudiatorModelType {

    SMALL(new SmallExperimentModelGenerator()),
    EXPERIMENT(new LargeModelGenerator()),
    CLOUD_HARMONY(new FileCachedModelGenerator(new CloudHarmony()));

    private final ModelGenerator modelGenerator;
    private Set<NodeCandidate> candidates = null;
    private CloudiatorModel model = null;

    CloudiatorModelType(ModelGenerator modelGenerator) {
      this.modelGenerator = modelGenerator;
    }

    public CloudiatorModel getCloudiatorModel() {
      if (model == null) {
        try {
          model = modelGenerator.generateModel("test");
        } catch (ModelGenerationException e) {
          throw new IllegalStateException(e);
        }
      }
      return model;
    }

    public Set<NodeCandidate> getCandidates() {
      if (this.candidates == null) {
        this.candidates = new ConsistentNodeGenerator(
            new DefaultNodeGenerator(ExperimentCSP.NODE_CANDIDATE_FACTORY,
                getCloudiatorModel()),
            ConstraintChecker.create(ExperimentCSP.CSP)).get();
      }
      return candidates;
    }
  }

}
