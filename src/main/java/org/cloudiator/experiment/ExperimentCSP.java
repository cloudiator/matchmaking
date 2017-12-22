package org.cloudiator.experiment;

import java.util.HashSet;
import org.cloudiator.ocl.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.ocl.OclCsp;

public class ExperimentCSP {

  public static final NodeCandidateFactory NODE_CANDIDATE_FACTORY = new NodeCandidateFactory();

  public static OclCsp CSP = OclCsp.ofConstraints(
      new HashSet<String>() {{
        add("nodes->exists(location.geoLocation.country = 'DE')");
        add("nodes->forAll(n | n.hardware.cores >= 2)");
        add("nodes->isUnique(n | n.location.geoLocation.country)");
        add("nodes->forAll(n | n.hardware.ram >= 1024)");
        add("nodes->forAll(n | n.hardware.cores >= 4 implies n.hardware.ram >= 4096)");
        add("nodes->forAll(n | n.image.operatingSystem.family = OSFamily::UBUNTU)");
        add("nodes->select(n | n.hardware.cores >= 4)->size() >= 2");
        add("nodes.hardware.cores->sum() >= 15");
      }}
  );

}
