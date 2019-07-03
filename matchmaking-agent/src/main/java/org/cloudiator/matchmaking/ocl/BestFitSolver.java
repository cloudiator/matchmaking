package org.cloudiator.matchmaking.ocl;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.domain.Solver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BestFitSolver implements Solver {

  private static final Logger LOGGER = LoggerFactory.getLogger(BestFitSolver.class);

  @Override
  public Solution solve(OclCsp oclCsp, NodeCandidates nodeCandidates,
      @Nullable Solution existingSolution, @Nullable Integer targetNodeSize) {

    if (existingSolution != null) {
      throw new UnsupportedOperationException(
          String.format("%s currently does not support importing existing solutions.", this));
    }

    SolutionGenerator solutionGenerator = new BaseSolutionGenerator(nodeCandidates);
    if (!oclCsp.getQuotaSet().quotaSet().isEmpty()) {
      solutionGenerator = new QuotaAwareSolutionGenerator(oclCsp.getQuotaSet(), solutionGenerator);
    }

    ConstraintChecker constraintChecker = ConstraintChecker.create(oclCsp);

    if (targetNodeSize == null) {
      targetNodeSize = 1;
    }
    while (!Thread.currentThread().isInterrupted()) {
      final BestFitInternal bestFitInternal = new BestFitInternal(solutionGenerator,
          constraintChecker, 100, targetNodeSize);
      LOGGER.debug(String.format("Using %s to solve", bestFitInternal));
      final Solution solution = bestFitInternal.solve();
      if (!solution.noSolution()) {
        return solution;
      }
      targetNodeSize++;
    }
    return Solution.EMPTY_SOLUTION;
  }

  private static class BestFitInternal {

    private final SolutionGenerator solutionGenerator;
    private final ConstraintChecker constraintChecker;
    private int limit;
    private int targetNodeSize;

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("limit", limit)
          .add("targetNodeSize", targetNodeSize)
          .toString();
    }

    private BestFitInternal(SolutionGenerator solutionGenerator,
        ConstraintChecker constraintChecker,
        int limit, int targetNodeSize) {
      this.solutionGenerator = solutionGenerator;
      this.constraintChecker = constraintChecker;
      this.limit = limit;
      this.targetNodeSize = targetNodeSize;
    }

    public Solution solve() {
      int limitToUse = limit;
      List<Solution> generation = new ArrayList<>();
      long startGeneration = System.currentTimeMillis();
      solutionGenerator.generateInitialSolutions().stream().sorted().forEach(generation::add);
      long endGeneration = System.currentTimeMillis();
      LOGGER
          .debug(String.format("%s took %s ms to generate %s initial solutions", this,
              endGeneration - startGeneration,
              generation.size()));
      int minViolations = Integer.MAX_VALUE;

      while (!generation.isEmpty() && !Thread.currentThread().isInterrupted()) {
        List<Solution> nextGeneration = new ArrayList<>();
        int nodeSize = generation.stream().findFirst().get().getNodeCandidates().size();
        LOGGER
            .debug(String.format("%s is currently using the target node size %s.", this, nodeSize));
        for (Solution solution : generation) {
          int violations = constraintChecker.check(solution.getNodeCandidates());
          if (violations == 0) {
            if (targetNodeSize <= nodeSize) {
              solution.setSolver(BestFitSolver.class);
              return solution;
            } else {
              limitToUse = 1;
            }
          }
          if (violations < minViolations) {
            nextGeneration.clear();
            minViolations = violations;
          }
          if (violations == minViolations) {
            nextGeneration.add(solution);
          }
        }
        generation.clear();

        long startChildGeneration = System.currentTimeMillis();
        nextGeneration.stream().sorted().limit(limitToUse)
            .flatMap(s -> solutionGenerator.getChilds(s).stream()).sorted()
            .forEach(generation::add);
        long endChildGeneration = System.currentTimeMillis();
        LOGGER
            .debug(String.format("%s took %s ms to generate %s child generations", this,
                endChildGeneration - startChildGeneration,
                generation.size()));
      }

      return Solution.EMPTY_SOLUTION;

    }

  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
