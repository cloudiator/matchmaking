package experiment;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.HashSet;
import java.util.Set;
import org.cloudiator.ocl.ConstraintSatisfactionProblem;
import org.cloudiator.ocl.Solver;
import org.eclipse.ocl.pivot.utilities.ParserException;

public class ExperimentRunner {

  private static final Injector INJECTOR = Guice.createInjector(new ExperimentModule());

  public static void main(String[] args) {

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
    Solver solver = INJECTOR.getInstance(Solver.class);

    try {
      solver.solve(csp, "blub");
    } catch (ParserException e) {
      e.printStackTrace();
    }


  }
}
