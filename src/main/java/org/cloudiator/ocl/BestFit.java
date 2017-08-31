package org.cloudiator.ocl;

import java.util.ArrayList;
import java.util.List;

public class BestFit {

  private final SolutionGenerator solutionGenerator;
  private final ConstraintChecker constraintChecker;
  private int limit;
  private int targetNodeSize;
  public static final int DYNAMIC_LIMIT = -1;

  public BestFit(SolutionGenerator solutionGenerator, ConstraintChecker constraintChecker,
      int limit, int targetNodeSize) {
    this.solutionGenerator = solutionGenerator;
    this.constraintChecker = constraintChecker;
    this.limit = limit;
    this.targetNodeSize = targetNodeSize;
  }

  public Solution solve() {
    int limitToUse = limit;
    System.out.println(targetNodeSize);
    List<Solution> generation = new ArrayList<>();
    solutionGenerator.generateInitialSolutions().stream().sorted().forEach(generation::add);
    int minViolations = Integer.MAX_VALUE;

    while (!generation.isEmpty()) {
      List<Solution> nextGeneration = new ArrayList<>();
      int nodeSize = generation.stream().findFirst().get().getList().size();
      System.out.println("Current nodeSize " + nodeSize);
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
          System.out.println(
              "Minimum violations is changing from " + minViolations + " to " + violations);
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
      System.out.println("Size of new generation: " + generation.size());
    }

    return null;

  }

}
