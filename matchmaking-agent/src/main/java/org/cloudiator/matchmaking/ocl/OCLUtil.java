package org.cloudiator.matchmaking.ocl;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.utilities.OCL;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.eclipse.ocl.pivot.utilities.Query;
import org.eclipse.ocl.xtext.essentialocl.EssentialOCLStandaloneSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OCLUtil {

  private static OCL ocl = OCL.newInstance(OCL.CLASS_PATH);
  private static final Logger LOGGER = LoggerFactory.getLogger(OCLUtil.class);

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
    try {
      return getOcl().createInvariant(contextElement, expression);
    } catch (Exception first) {
      LOGGER.warn(String
          .format(
              "Error while creating invariant for contextElement %s and expression %s. Retrying.",
              contextElement, expression), first);
      try {
        return getOcl().createInvariant(contextElement, expression);
      } catch (Exception second) {
        LOGGER.error(String
            .format(
                "Retry creating of invariant for contextElement %s and expression %s also failed. Giving up with exception.",
                contextElement, expression), second);
        throw second;
      }
    }
  }

  public static Query createQuery(ExpressionInOCL constraint) {
    return getOcl().createQuery(constraint);
  }

}
