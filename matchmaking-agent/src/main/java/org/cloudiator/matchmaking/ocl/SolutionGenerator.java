package org.cloudiator.matchmaking.ocl;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SolutionGenerator {

  private final NodeGenerator nodeGenerator;
  private volatile Set<NodeCandidate> nodeCandidates;

  public SolutionGenerator(NodeGenerator nodeGenerator) {
    this.nodeGenerator = nodeGenerator;
  }

  public void clean() {
    nodeCandidates.clear();
  }

  public int nodeCandidatesSize() {
    return nodeCandidates().size();
  }

  private Set<NodeCandidate> nodeCandidates() {
    if (this.nodeCandidates == null || this.nodeCandidates.isEmpty()) {
      this.nodeCandidates = nodeGenerator.getPossibleNodes();
    }
    return this.nodeCandidates;
  }

  public List<Solution> generateInitialSolutions() {
    List<Solution> candidates = nodeCandidates().stream()
        .map(node -> Solution.of(Collections.singletonList(node)))
        .collect(Collectors.toList());
    return candidates;
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
