package org.cloudiator.matchmaking.ocl;

import org.eclipse.ocl.pivot.utilities.OCL;
import org.eclipse.ocl.xtext.essentialocl.EssentialOCLStandaloneSetup;

public class OCLHelper {

  private static OCL ocl = OCL.newInstance(OCL.CLASS_PATH);

  static {
    EssentialOCLStandaloneSetup.doSetup();
  }

  public static OCL getOcl() {
    return ocl;
  }

}
