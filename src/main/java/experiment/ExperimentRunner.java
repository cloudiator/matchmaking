package experiment;

import choco.ChocoSolver;
import choco.TimeLimit;
import com.google.inject.Guice;
import com.google.inject.Injector;
import experiment.Experiment.CloudiatorModelType;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.cloudiator.ocl.ConstraintSatisfactionProblem;
import org.cloudiator.ocl.Solution;

public class ExperimentRunner {

  private static final Injector INJECTOR = Guice.createInjector(new ExperimentModule());

  public static void main(String[] args)
      throws FileNotFoundException, UnsupportedEncodingException {

    Set<String> constraints = new HashSet<>();

    ConstraintSatisfactionProblem csp = new ConstraintSatisfactionProblem(constraints);

    final Data data = new Data();

    List<Experiment> experiments = new ArrayList<>();
    for (int i = 2; i <= 15; i++) {
      for (CloudiatorModelType cloudiatorModelType : CloudiatorModelType.values()) {
        experiments.add(
            new Experiment(new TimeLimit(TimeUnit.MINUTES, 1), i, 10, false,
                cloudiatorModelType)
        );
        experiments.add(
            new Experiment(new TimeLimit(TimeUnit.MINUTES, 5), i, 10, false,
                cloudiatorModelType)
        );
        experiments.add(
            new Experiment(new TimeLimit(TimeUnit.MINUTES, 10), i, 10, false,
                cloudiatorModelType)
        );
        //experiments.add(
        //    new Experiment(new TimeLimit(TimeUnit.MINUTES, 10), i, 10, true,
        //        cloudiatorModelType)
        //);
      }
    }

    PrintWriter cloudHarmonyWriter = new PrintWriter("solutionsCloudHarmony", "UTF-8");
    PrintWriter experimentModelWriter = new PrintWriter("solutionsLarge", "UTF-8");
    PrintWriter smallModelWriter = new PrintWriter("solutionsSmall", "UTF-8");

    for (Experiment experiment : experiments) {
      ChocoSolver chocoSolver = new ChocoSolver(
          experiment.getCloudiatorModelType().getCandidates());

      for (int rep = 0; rep < experiment.getRepetitions(); rep++) {
        Solution solution = null;
        if (!experiment.isIterative()) {
          solution = chocoSolver.solveDirect(experiment.getNodeSize(), experiment.getLimit());
        } else {
          solution = chocoSolver.solveIteratively(experiment.getNodeSize(), experiment.getLimit());
        }
        if (solution != null) {
          experiment.addSolution(solution);
        }
      }

      PrintWriter writer = null;

      switch (experiment.getCloudiatorModelType()) {
        case CLOUD_HARMONY:
          writer = cloudHarmonyWriter;
          break;
        case EXPERIMENT:
          writer = experimentModelWriter;
          break;
        case SMALL:
          writer = smallModelWriter;
          break;
        default:
          throw new IllegalArgumentException();
      }

      if (experiment.hasSolution()) {

        writer.println(
            experiment.isIterative() + " " + experiment.getLimit().toString() + " " + experiment
                .getCloudiatorModelType().name() + " " + experiment.getNodeSize() + " "
                + experiment.getCostStatistics().getAverage() + " "
                + experiment.timeStatistics().getAverage() + " " + experiment.optimal()
                .isPresent());


      } else {
        writer.println(
            experiment.isIterative() + " " + experiment.getLimit().toString() + " " + experiment
                .getCloudiatorModelType().name() + " " + experiment.getNodeSize() + " "
                + "No Solution");
      }

      writer.flush();
    }

    experimentModelWriter.close();
    cloudHarmonyWriter.close();

  }
}
