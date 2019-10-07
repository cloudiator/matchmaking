package org.cloudiator.matchmaking.ocl;

import de.uniulm.omi.cloudiator.sword.domain.QuotaSet;
import java.util.Collections;
import org.eclipse.ocl.pivot.utilities.ParserException;

public class OclCspTest {

  @org.junit.Test
  public void ofConstraints() throws ParserException {
    OclCsp.ofConstraints(OclTestCsp.TEST_CSP, Collections.emptyList(), QuotaSet.EMPTY, 1);
  }

  @org.junit.Test
  public void ofRequirements() throws ParserException {
    OclCsp.ofRequirements(TestAttributeRequirements.TEST_REQUIREMENTS, Collections.emptyList(),
        QuotaSet.EMPTY, 1);
  }
}
