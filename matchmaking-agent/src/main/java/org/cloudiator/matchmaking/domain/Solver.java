package org.cloudiator.matchmaking.domain;

import org.cloudiator.matchmaking.ocl.NodeCandidates;
import org.cloudiator.matchmaking.ocl.OclCsp;
import org.cloudiator.matchmaking.domain.Solution;

public interface Solver {

  Solution solve(OclCsp oclCsp, NodeCandidates nodeCandidates);

}
