package org.cloudiator.ocl;

import java.util.List;
import java.util.stream.Collectors;

public class BackTracking {

  private final SolutionGenerator solutionGenerator;
  private final ConstraintChecker constraintChecker;
  private final int targetNodeSize;

  public BackTracking(SolutionGenerator solutionGenerator,
      ConstraintChecker constraintChecker, int targetNodeSize) {
    this.solutionGenerator = solutionGenerator;
    this.constraintChecker = constraintChecker;
    this.targetNodeSize = targetNodeSize;
  }

  public Solution solve() {

    List<Solution> initialSolutions = solutionGenerator.generateInitialSolutions().stream().sorted()
        .collect(
            Collectors.toList());

    for (Solution solution : initialSolutions) {
      int violations = constraintChecker.check(solution.getList());
      for (Solution childs : solutionGenerator.getChilds(solution).stream().sorted()
          .collect(Collectors.toList())) {

      }
    }

    return null;
  }
}
