package org.cloudiator.matchmaking.ocl;

import java.util.Collections;
import java.util.List;
import org.cloudiator.matchmaking.domain.Solution;

public class ExistingSolutionGenerator implements SolutionGenerator {

  private final Solution existingSolution;
  private final SolutionGenerator delegate;

  public ExistingSolutionGenerator(Solution existingSolution,
      SolutionGenerator delegate) {
    this.existingSolution = existingSolution;
    this.delegate = delegate;
  }

  @Override
  public int nodeCandidatesSize() {
    return existingSolution.nodeSize();
  }

  @Override
  public List<Solution> generateInitialSolutions() {
    return Collections.singletonList(existingSolution);
  }

  @Override
  public List<Solution> getChilds(Solution solution) {
    return delegate.getChilds(solution);
  }
}
