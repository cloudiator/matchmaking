package org.cloudiator.matchmaking.ocl;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.Solution;

public class BaseSolutionGenerator implements SolutionGenerator {

  private volatile Set<NodeCandidate> nodeCandidates;

  public BaseSolutionGenerator(Set<NodeCandidate> nodeCandidates) {
    this.nodeCandidates = nodeCandidates;
  }

  public void clean() {
    nodeCandidates.clear();
  }

  @Override
  public int nodeCandidatesSize() {
    return nodeCandidates().size();
  }

  private Set<NodeCandidate> nodeCandidates() {
    return this.nodeCandidates;
  }

  @Override
  public List<Solution> generateInitialSolutions() {
    return nodeCandidates().stream()
        .map(node -> Solution.of(Collections.singletonList(node)))
        .collect(Collectors.toList());
  }

  @Override
  public List<Solution> getChilds(Solution solution) {

    Set<NodeCandidate> nodeCandidates = nodeCandidates();
    List<Solution> solutions = new ArrayList<>(nodeCandidates.size());
    for (NodeCandidate nodeCandiate : nodeCandidates) {
      solutions.add(Solution.of(Lists.asList(nodeCandiate,
          solution.getNodeCandidates().toArray(new NodeCandidate[solution.getNodeCandidates().size()]))));
    }
    return solutions;
  }

}
