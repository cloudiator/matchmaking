package org.cloudiator.matchmaking.ocl;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.domain.Solution;

public class BreathFirst {

  private final SolutionGenerator solutionGenerator;
  private final ConstraintChecker constraintChecker;
  private final Queue<Solution> solutions = new ArrayDeque<>();
  private final int targetNodeSize;

  public BreathFirst(SolutionGenerator solutionGenerator, ConstraintChecker constraintChecker,
      int targetNodeSize) {
    this.solutionGenerator = solutionGenerator;
    this.constraintChecker = constraintChecker;
    this.targetNodeSize = targetNodeSize;
  }

  public Solution solve() {
    solutions.addAll(solutionGenerator.generateInitialSolutions().stream().sorted()
        .collect(Collectors.toList()));
    int minViolations = Integer.MAX_VALUE;
    while (!solutions.isEmpty()) {
      Solution solution = solutions.poll();
      int violations = constraintChecker.check(solution.getList());
      if (violations == 0 && solution.nodeSize() >= targetNodeSize) {
        return solution;
      }
      if (violations <= minViolations) {
        minViolations = violations;
        solutions.addAll(
            solutionGenerator.getChilds(solution).stream().sorted().collect(Collectors.toList()));
      }
    }
    return null;
  }


}
