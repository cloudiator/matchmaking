package org.cloudiator.choco;

import org.cloudiator.experiment.Experiment.CloudiatorModelType;
import org.cloudiator.experiment.ExperimentCSP;

public class ChocoRunner {

  public static void main(String[] args) {
    new ChocoSolver().solve(ExperimentCSP.CSP, CloudiatorModelType.SMALL.getCloudiatorModel());
  }

}
