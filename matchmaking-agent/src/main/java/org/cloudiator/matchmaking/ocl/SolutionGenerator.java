package org.cloudiator.matchmaking.ocl;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.Solution;

public class SolutionGenerator {

  private volatile Set<NodeCandidate> nodeCandidates;

  public SolutionGenerator(Set<NodeCandidate> nodeCandidates) {
    this.nodeCandidates = nodeCandidates;
  }

  public void clean() {
    nodeCandidates.clear();
  }

  public int nodeCandidatesSize() {
    return nodeCandidates().size();
  }

  private Set<NodeCandidate> nodeCandidates() {
    return this.nodeCandidates;
  }

  public List<Solution> generateInitialSolutions() {
    return nodeCandidates().stream()
        .map(node -> Solution.of(Collections.singletonList(node)))
        .collect(Collectors.toList());
  }

  public List<Solution> getChilds(Solution solution) {

    Set<NodeCandidate> nodeCandidates = nodeCandidates();
    List<Solution> solutions = new ArrayList<>(nodeCandidates.size());
    for (NodeCandidate nodeCandiate : nodeCandidates) {
      solutions.add(Solution.of(Lists.asList(nodeCandiate,
          solution.getList().toArray(new NodeCandidate[solution.getList().size()]))));
    }
    return solutions;
  }

}
