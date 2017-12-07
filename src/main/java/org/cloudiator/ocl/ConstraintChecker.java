package org.cloudiator.ocl;

import cloudiator.CloudiatorPackage;
import cloudiator.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.utilities.OCL;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.eclipse.ocl.pivot.utilities.Query;
import org.eclipse.ocl.xtext.essentialocl.EssentialOCLStandaloneSetup;

public class ConstraintChecker {

  static {
    EssentialOCLStandaloneSetup.doSetup();
  }

  private static OCL ocl = OCL.newInstance(OCL.CLASS_PATH);

  private final Set<Query> forAllQueries;
  private final Set<Query> otherQueries;
  private final Component component;

  public ConstraintChecker(OclCsp csp) throws ParserException {
    forAllQueries = new HashSet<>();
    otherQueries = new HashSet<>();

    for (String constraint : csp.getConstraints()) {
      ExpressionInOCL expression = ocl
          .createInvariant(CloudiatorPackage.eINSTANCE.getComponent(), constraint);
      Query query = ocl.createQuery(expression);
      if (constraint.contains("forAll")) {
        forAllQueries.add(query);
      } else {
        otherQueries.add(query);
      }
    }

    component = CloudiatorPackage.eINSTANCE.getCloudiatorFactory().createComponent();
    component.setName(UUID.randomUUID().toString());
  }

  public boolean consistent(NodeCandidate node) {
    synchronized (ConstraintChecker.class) {
      component.getNodes().clear();
      component.getNodes().add(node.getNode());

      return forAllQueries.stream().parallel().allMatch(t -> t.checkEcore(component));
    }
  }

  public int check(List<NodeCandidate> nodes) {
    synchronized (ConstraintChecker.class) {
      component.getNodes().clear();

      for (NodeCandidate node : nodes) {
        component.getNodes().add(node.getNode());
      }

      return (int) otherQueries.stream().parallel().filter(q -> !q.checkEcore(component)).count();
    }
  }

}
