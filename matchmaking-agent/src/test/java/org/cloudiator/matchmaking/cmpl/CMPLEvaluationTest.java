package org.cloudiator.matchmaking.cmpl;

import cloudiator.CloudiatorModel;
import de.uniulm.omi.cloudiator.sword.domain.QuotaSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.ocl.ConsistentNodeGenerator;
import org.cloudiator.matchmaking.ocl.ConstraintChecker;
import org.cloudiator.matchmaking.ocl.DefaultNodeGenerator;
import org.cloudiator.matchmaking.ocl.ExampleModel;
import org.cloudiator.matchmaking.ocl.OclCsp;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.junit.Test;

public class CMPLEvaluationTest {

  private static final NodeCandidateFactory nodeCandidateFactory = NodeCandidateFactory.create();

  /*
        - constraint: nodes->size() >= NODESIZE
          type: OclRequirement
        - constraint: nodes->select(n | n.cloud.type = CloudType::PUBLIC)->size() = NODESIZE / 2
          type: OclRequirement
        - constraint: nodes->select(n | n.cloud.type = CloudType::PRIVATE)->size() = NODESIZE / 2
   */

  @Test
  public void test() throws ParserException {
    Set<String> constraints = new HashSet<String>() {{
      add("nodes->size() >= 20");
      add("nodes->select(n | n.cloud.type = CloudType::PUBLIC)->size() = 20 / 2");
      add("nodes->select(n | n.cloud.type = CloudType::PRIVATE)->size() = 20 / 2");
    }};

    final OclCsp oclCsp = OclCsp
        .ofConstraints(constraints, Collections.emptyList(), QuotaSet.EMPTY, null);

    final CloudiatorModel cloudiatorModel = ExampleModel.testModel();
    final ConsistentNodeGenerator nodeGenerator = new ConsistentNodeGenerator(
        new DefaultNodeGenerator(nodeCandidateFactory, cloudiatorModel, null),
        ConstraintChecker.create(oclCsp));

    final Solution solution = new CMPLSolver().solve(oclCsp, nodeGenerator.get(), null, null);
  }

}
