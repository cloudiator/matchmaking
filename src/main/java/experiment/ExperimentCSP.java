package experiment;

import java.util.HashSet;
import org.cloudiator.ocl.ConstraintSatisfactionProblem;
import org.cloudiator.ocl.NodeCandidate.NodeCandidateFactory;

public class ExperimentCSP {

  public static final NodeCandidateFactory NODE_CANDIDATE_FACTORY = new NodeCandidateFactory();

  public static ConstraintSatisfactionProblem CSP = new ConstraintSatisfactionProblem(
      new HashSet<String>() {{
        add("nodes->exists(location.country = 'DE')");
        add("nodes->forAll(n | n.hardware.cores >= 2)");
        add("nodes->isUnique(n | n.location.country)");
        add("nodes->forAll(n | n.hardware.ram >= 1024)");
        add("nodes->forAll(n | n.hardware.cores >= 4 implies n.hardware.ram >= 4096)");
        add("nodes->forAll(n | n.image.operatingSystem.family = OSFamily::UBUNTU)");
        add("nodes->select(n | n.hardware.cores >= 4)->size() >= 2");
        add("nodes.hardware.cores->sum() >= 15");
      }}
  );

}
