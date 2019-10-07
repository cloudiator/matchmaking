package org.cloudiator.matchmaking.ocl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import cloudiator.CloudiatorModel;
import de.uniulm.omi.cloudiator.sword.domain.QuotaSet;
import java.util.Collections;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.junit.Test;

public class ConsistentNodeGeneratorTest {

  @Test
  public void test() throws ParserException {

    CloudiatorModel testModel = ExampleModel.testModel();
    final String forAllCountry = "nodes->forAll(location.geoLocation.country = 'DE')";
    final OclCsp oclCsp = OclCsp.ofConstraints(Collections.singleton(forAllCountry),Collections.emptyList(),
        QuotaSet.EMPTY, 1);
    NodeGenerator nodeGenerator = new DefaultNodeGenerator(NodeCandidateFactory.create(),
        testModel, new ByonUpdater());
    ConstraintChecker constraintChecker = ConstraintChecker.create(oclCsp);
    ConsistentNodeGenerator consistentNodeGenerator = new ConsistentNodeGenerator(nodeGenerator,
        constraintChecker);

    final NodeCandidates nodeCandidates = consistentNodeGenerator.get();

    for (NodeCandidate nodeCandidate : nodeCandidates) {
      assertThat(nodeCandidate.getLocation().getGeoLocation().getCountry(), equalTo("DE"));
    }

  }

}
