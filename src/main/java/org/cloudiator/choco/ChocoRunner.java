package org.cloudiator.choco;

import org.cloudiator.experiment.Experiment.CloudiatorModelType;
import org.cloudiator.experiment.ExperimentCSP;
import org.cloudiator.ocl.ConsistentNodeGenerator;
import org.cloudiator.ocl.ConstraintChecker;
import org.cloudiator.ocl.DefaultNodeGenerator;
import org.cloudiator.ocl.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.ocl.NodeGenerator;

public class ChocoRunner {

  public static void main(String[] args) {

    NodeGenerator nodeGenerator = new ConsistentNodeGenerator(
        new DefaultNodeGenerator(NodeCandidateFactory.create(),
            CloudiatorModelType.CLOUD_HARMONY.getCloudiatorModel()),
        ConstraintChecker.create(ExperimentCSP.CSP));

    new ChocoSolver().solve(ExperimentCSP.CSP, nodeGenerator.getPossibleNodes());
  }

}
