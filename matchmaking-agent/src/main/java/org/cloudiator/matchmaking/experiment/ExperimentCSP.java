package org.cloudiator.matchmaking.experiment;

import de.uniulm.omi.cloudiator.sword.domain.QuotaSet;
import java.util.Collections;
import java.util.HashSet;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.matchmaking.ocl.OclCsp;
import org.eclipse.ocl.pivot.utilities.ParserException;

public class ExperimentCSP {

  public static final NodeCandidateFactory NODE_CANDIDATE_FACTORY = NodeCandidateFactory.create();

  public static OclCsp CSP;

  static {
    try {
      CSP = OclCsp.ofConstraints(
          new HashSet<String>() {{
            //add("nodes->exists(location.geoLocation.country = 'DE')");
            add("nodes->forAll(n | n.hardware.cores >= 2)");
            //add("nodes->isUnique(n | n.location.geoLocation.country)");
            add("nodes->forAll(n | n.hardware.ram >= 1024)");
            add("nodes->forAll(n | n.hardware.ram < 8000)");
            add("nodes->forAll(n | n.hardware.cores >= 4 implies n.hardware.ram >= 4096)");
            add("nodes->forAll(n | n.image.operatingSystem.family = OSFamily::UBUNTU)");
            add("nodes->select(n | n.hardware.cores >= 4)->size() = 2");
            add("nodes.hardware.cores->sum() >= 15");
          }}, Collections.emptyList(), QuotaSet.EMPTY, 1
      );
    } catch (ParserException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

}
