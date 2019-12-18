package org.cloudiator.matchmaking.choco;

import cloudiator.CloudiatorModel;
import de.uniulm.omi.cloudiator.sword.domain.QuotaSet;
import java.util.Collections;
import java.util.HashSet;
import org.chocosolver.solver.Model;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.matchmaking.ocl.ConsistentNodeGenerator;
import org.cloudiator.matchmaking.ocl.ConstraintChecker;
import org.cloudiator.matchmaking.ocl.DefaultNodeGenerator;
import org.cloudiator.matchmaking.ocl.ExampleModel;
import org.cloudiator.matchmaking.ocl.OclCsp;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.junit.Test;

public class ModelGenerationTest {

  private static final NodeCandidateFactory nodeCandidateFactory = NodeCandidateFactory.create();

  public static final HashSet<String> CSP = new HashSet<String>() {{
    add("nodes->select(n | n.cloud.type = CloudType::PUBLIC)->size() = 40 / 2");
  }};

  @Test
  public void visit() throws ParserException {
    final CloudiatorModel cloudiatorModel = ExampleModel.testModel();
    final OclCsp oclCsp = OclCsp.ofConstraints(CSP, Collections.emptyList(), QuotaSet.EMPTY, null);

    final ConsistentNodeGenerator nodeGenerator = new ConsistentNodeGenerator(
        new DefaultNodeGenerator(nodeCandidateFactory, cloudiatorModel, null),
        ConstraintChecker.create(oclCsp));

    SolvingModelGenerator solvingModelGenerator = new SolvingModelGenerator();
    final CloudiatorModel solvingModel = solvingModelGenerator.apply(nodeGenerator.get());

    final ModelGenerationContext modelGenerationContext = new ModelGenerationContext(
        solvingModel, new Model(), 20,
        oclCsp,
        null);

    ChocoModelGeneration.visit(modelGenerationContext);
  }
}
