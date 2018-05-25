package org.cloudiator.matchmaking;

import org.cloudiator.matchmaking.ocl.NodeCandidates;
import org.cloudiator.matchmaking.ocl.OclCsp;
import org.cloudiator.matchmaking.ocl.Solution;

public interface Solver {

  public Solution solve(OclCsp oclCsp, NodeCandidates nodeCandidates);

}
