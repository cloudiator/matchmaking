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
    constraints.add("nodes->exists(location.country = 'DE')");
    constraints.add("nodes->forAll(n | n.hardware.cores >= 2)");
    constraints.add("nodes->isUnique(n | n.location.country)");
    constraints.add("nodes->forAll(n | n.hardware.ram >= 1024)");
    constraints.add("nodes->forAll(n | n.hardware.cores >= 4 implies n.hardware.ram >= 4096)");
    constraints.add("nodes->forAll(n | n.image.operatingSystem.family = OSFamily::UBUNTU)");
    //constraints.add("nodes->forAll(n | n.image.operatingSystem.version = '1')");
    //constraints
    //    .add("nodes->forAll(n | n.image.operatingSystem.architecture = OSArchitecture::AMD64)");
    constraints.add("nodes->select(n | n.hardware.cores > 4)->size() >= 2");
    constraints.add("nodes.hardware.cores->sum() >= 15");

    ConstraintSatisfactionProblem csp = new ConstraintSatisfactionProblem(constraints);

    final Data data = new Data();

    List<Experiment> experiments = new ArrayList<>();
    for (int i = 2; i <= 15; i++) {
      //for (CloudiatorModelType cloudiatorModelType : CloudiatorModelType.values()) {
        experiments.add(
            new Experiment(new TimeLimit(TimeUnit.DAYS, 1), i, 1, false, CloudiatorModelType.CLOUD_HARMONY));
      //}
    }

    PrintWriter cloudHarmonyWriter = new PrintWriter("optimalSolutionsCloudHarmony", "UTF-8");
    PrintWriter experimentModelWriter = new PrintWriter("optimalSolutionsExperiment", "UTF-8");

    for (Experiment experiment : experiments) {
      ChocoSolver chocoSolver = new ChocoSolver(csp,
          experiment.getCloudiatorModelType().getCloudiatorModel());

      for (int rep = 0; rep < experiment.getRepetitions(); rep++) {
        Solution solution = null;
        if (!experiment.isIterative()) {
          solution = chocoSolver.solveOptimal(experiment.getNodeSize(), experiment.getLimit());
        } else {
          chocoSolver.solveIteratively(experiment.getNodeSize(), experiment.getLimit());
        }
        experiment.addSolution(solution);
      }
      if (experiment.optimal().isPresent()) {
        PrintWriter writer = null;
        if (experiment.getCloudiatorModelType().equals(CloudiatorModelType.CLOUD_HARMONY)) {
          writer = cloudHarmonyWriter;
        } else {
          writer = experimentModelWriter;
        }
        Solution solution = experiment.optimal().get();
        System.out.println(solution);
        writer.println(solution.nodeSize() + " " + solution.getCosts() + " "
            + solution.getTime());
        writer.flush();
      } else {
        System.out.println("No optimal solution found.");
      }
    }

    experimentModelWriter.close();
    cloudHarmonyWriter.close();

  }
}
