package org.cloudiator.matchmaking.ocl;

import java.util.Set;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.domain.NodeCandidate;

public class ConsistentNodeGenerator implements NodeGenerator {

  private final NodeGenerator nodeGenerator;
  private final ConstraintChecker constraintChecker;

  public ConsistentNodeGenerator(NodeGenerator nodeGenerator, ConstraintChecker checker) {
    this.nodeGenerator = nodeGenerator;
    this.constraintChecker = checker;
  }

  @Override
  public NodeCandidates get() {
    Set<NodeCandidate> consistentNodes =
        nodeGenerator.get().stream().filter(this.constraintChecker::consistent)
            .collect(Collectors.toSet());
    System.out.println(
        String.format("%s generated %s consistent nodes", this, consistentNodes.size()));
    return NodeCandidates.of(consistentNodes);
  }

}
