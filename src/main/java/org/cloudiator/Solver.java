package org.cloudiator;

import org.cloudiator.ocl.NodeCandidates;
import org.cloudiator.ocl.OclCsp;
import org.cloudiator.ocl.Solution;

public interface Solver {

  public Solution solve(OclCsp oclCsp, NodeCandidates nodeCandidates);

}
