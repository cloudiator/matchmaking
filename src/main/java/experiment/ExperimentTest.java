package experiment;

import choco.ChocoSolver;
import choco.TimeLimit;
import experiment.Experiment.CloudiatorModelType;
import java.util.concurrent.TimeUnit;
import org.cloudiator.ocl.Solution;

public class ExperimentTest {

  public static void main(String[] args) {

    ChocoSolver chocoSolver = new ChocoSolver(CloudiatorModelType.CLOUD_HARMONY.getCandidates());

    //final Solution solution = chocoSolver.solveIteratively(8, new TimeLimit(TimeUnit.DAYS, 1));

    //System.out.println(solution);

    final Solution solution1 = chocoSolver.solveDirect(8, new TimeLimit(TimeUnit.DAYS, 1));

    System.out.println(solution1);


  }

}
