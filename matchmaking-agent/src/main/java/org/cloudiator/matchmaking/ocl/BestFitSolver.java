package org.cloudiator.matchmaking.ocl;

import java.util.ArrayList;
import java.util.List;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.domain.Solver;

public class BestFitSolver implements Solver {

  @Override
  public Solution solve(OclCsp oclCsp, NodeCandidates nodeCandidates) {

    SolutionGenerator solutionGenerator = new SolutionGenerator(nodeCandidates);
    ConstraintChecker constraintChecker = ConstraintChecker.create(oclCsp);

    int targetNodeSize = 1;
    while (true) {
      final BestFitInternal bestFitInternal = new BestFitInternal(solutionGenerator,
          constraintChecker, 100, targetNodeSize);
      final Solution solution = bestFitInternal.solve();
      if (!solution.noSolution()) {
        return solution;
      }
      targetNodeSize++;
    }
  }

  private static class BestFitInternal {

    private final SolutionGenerator solutionGenerator;
    private final ConstraintChecker constraintChecker;
    private int limit;
    private int targetNodeSize;

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
      solutionGenerator.generateInitialSolutions().stream().sorted().forEach(generation::add);
      int minViolations = Integer.MAX_VALUE;

      while (!generation.isEmpty() && !Thread.currentThread().isInterrupted()) {
        List<Solution> nextGeneration = new ArrayList<>();
        int nodeSize = generation.stream().findFirst().get().getList().size();
        for (Solution solution : generation) {
          int violations = constraintChecker.check(solution.getList());
          if (violations == 0) {
            if (targetNodeSize <= nodeSize) {
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

        nextGeneration.stream().sorted().limit(limitToUse)
            .flatMap(s -> solutionGenerator.getChilds(s).stream()).sorted()
            .forEach(generation::add);
      }

      return Solution.EMPTY_SOLUTION;

    }

  }
}
