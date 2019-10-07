package org.cloudiator.matchmaking.ocl;

import java.util.List;
import org.cloudiator.matchmaking.domain.Solution;

public interface SolutionGenerator {

  int nodeCandidatesSize();

  List<Solution> generateInitialSolutions();

  List<Solution> getChilds(Solution solution);
}
