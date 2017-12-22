package org.cloudiator.choco;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.cloudiator.EMFUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.IteratorExp;
import org.eclipse.ocl.pivot.LiteralExp;
import org.eclipse.ocl.pivot.OCLExpression;
import org.eclipse.ocl.pivot.Operation;
import org.eclipse.ocl.pivot.OperationCallExp;
import org.eclipse.ocl.pivot.PropertyCallExp;

public class ConstraintHandler {

  private final ModelGenerationContext modelGenerationContext;
  private final EMFUtil emfUtil;

  private ConstraintHandler(ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
    this.emfUtil = EMFUtil.of(modelGenerationContext.getCloudiatorModel());
  }

  public static class ConstraintHandlerModelGenerationVisitor implements
      ModelGenerationContextVisitor {

    @Override
    public void visit(ModelGenerationContext modelGenerationContext) {
      new ConstraintHandler(modelGenerationContext).generate();
    }
  }

  public void generate() {
    for (ExpressionInOCL expressionInOCL : modelGenerationContext.getOclCsp().getConstraints()) {
      this.handleExpression(expressionInOCL.getOwnedBody());
    }
  }

  private void handleExpression(OCLExpression oclExpression) {
    if (oclExpression instanceof OperationCallExp) {
      generateConstraintFromOperationCallExp((OperationCallExp) oclExpression);
    } else if (oclExpression instanceof IteratorExp) {
      handleIteratorExp((IteratorExp) oclExpression);
    } else if (oclExpression instanceof PropertyCallExp) {
      getVariableForPropertyCallExp((PropertyCallExp) oclExpression, 1);
    } else {
      throw new AssertionError(
          "Unknown type of oclExpression: +" + oclExpression);
    }
  }

  private Constraint generateConstraintFromOperationCallExp(OperationCallExp operationCallExp) {

    switch (operationCallExp.getName()) {
      case "implies":
        break;
      case "=":
        break;
      case ">=":
        break;
      default:
        throw new AssertionError(String
            .format("Unknown Operation name %s: %s", operationCallExp.getName(), operationCallExp));

    }

    handleOperation(operationCallExp.getReferredOperation());
    System.out.println(operationCallExp);
    return null;
  }

  private String handleOperation(Operation operation) {
    return operation.getName();
  }

  private void handleIteratorExp(IteratorExp iteratorExp) {

    handleExpression(iteratorExp.getOwnedBody());

    switch (iteratorExp.getReferredIteration().getName()) {
      case "forAll":
        break;
      case "exists":
        break;
      case "isUnique":
        postIsUniqueConstraint(iteratorExp);
        break;
      default:
        throw new AssertionError(String.format("Unknown iteration expression %s: %s",
            iteratorExp.getReferredIteration().getName(), iteratorExp.getReferredIteration()));


    }

    System.out.println(iteratorExp);
  }

  private Variable getVariableForExp(OCLExpression oclExpression, int node) {
    if (oclExpression instanceof PropertyCallExp) {
      return getVariableForPropertyCallExp((PropertyCallExp) oclExpression, node);
    } else if (oclExpression instanceof LiteralExp) {
      return getVariableForLiteralExpression((LiteralExp) oclExpression, node);
    } else {
      throw new AssertionError("Can not retrieve a variable for the expression " + oclExpression);
    }
  }

  private Variable getVariableForPropertyCallExp(PropertyCallExp propertyCallExp, int node) {
    EAttribute eAttribute = (EAttribute) propertyCallExp.getReferredProperty().getESObject();
    return modelGenerationContext.getVariableStore().getVariables(node).get(eAttribute);
  }

  private Variable getVariableForLiteralExpression(LiteralExp literalExp, int node) {
    final EAttribute eAttribute = (EAttribute) literalExp.getESObject();
    return modelGenerationContext.getVariableStore().getVariables(node).get(eAttribute);
  }

  private void postIsUniqueConstraint(IteratorExp iteratorExp) {

    IntVar[] intVars = new IntVar[modelGenerationContext.nodeSize()];

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
      intVars[node - 1] = (IntVar) getVariableForExp(
          iteratorExp.getOwnedBody(), node);
    }
    modelGenerationContext.getModel().allDifferent(intVars).post();
  }


}
