package org.cloudiator.matchmaking.ocl;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.utilities.OCL;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.eclipse.ocl.pivot.utilities.Query;
import org.eclipse.ocl.xtext.essentialocl.EssentialOCLStandaloneSetup;

public class OCLUtil {

  private static OCL ocl = OCL.newInstance(OCL.CLASS_PATH);

  static {
    EssentialOCLStandaloneSetup.doSetup();
  }

  private OCLUtil() {
    throw new AssertionError("Do not instantiate");
  }

  private static OCL getOcl() {
    return ocl;
  }

  public static ExpressionInOCL createInvariant(EObject contextElement, String expression)
      throws ParserException {
    return getOcl().createInvariant(contextElement, expression);
  }

  public static Query createQuery(ExpressionInOCL constraint) {
    return getOcl().createQuery(constraint);
  }

}
