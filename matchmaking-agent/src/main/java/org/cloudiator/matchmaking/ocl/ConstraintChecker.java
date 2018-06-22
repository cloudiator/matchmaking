package org.cloudiator.matchmaking.ocl;

import cloudiator.CloudiatorPackage;
import cloudiator.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.eclipse.ocl.pivot.utilities.Query;

public class ConstraintChecker {

  private final Set<Query> forAllQueries;
  private final Set<Query> otherQueries;
  private final Component component;

  private ConstraintChecker(OclCsp csp) throws ParserException {
    forAllQueries = new HashSet<>();
    otherQueries = new HashSet<>();

    for (String constraint : csp.getUnparsedConstraints()) {
      ExpressionInOCL expression = OCLUtil
          .createInvariant(CloudiatorPackage.eINSTANCE.getComponent(), constraint);
      Query query = OCLUtil.createQuery(expression);
      if (constraint.contains("forAll")) {
        forAllQueries.add(query);
      } else {
        otherQueries.add(query);
      }
    }

    component = CloudiatorPackage.eINSTANCE.getCloudiatorFactory().createComponent();
    component.setName(UUID.randomUUID().toString());
  }

  public static ConstraintChecker create(OclCsp oclCsp) {
    try {
      return new ConstraintChecker(oclCsp);
    } catch (ParserException e) {
      throw new IllegalStateException(e);
    }
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
