package org.cloudiator.matchmaking.ocl;

import org.eclipse.ocl.pivot.utilities.ParserException;

public class OclCspTest {

  @org.junit.Test
  public void ofConstraints() throws ParserException {
    OclCsp.ofConstraints(OclTestCsp.TEST_CSP);
  }

  @org.junit.Test
  public void ofRequirements() {
  }
}
