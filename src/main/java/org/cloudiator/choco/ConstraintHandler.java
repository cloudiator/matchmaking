package org.cloudiator.choco;

import static com.google.common.base.Preconditions.checkState;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.cloudiator.EMFUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.ocl.pivot.EnumLiteralExp;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.IntegerLiteralExp;
import org.eclipse.ocl.pivot.IteratorExp;
import org.eclipse.ocl.pivot.LiteralExp;
import org.eclipse.ocl.pivot.NumericLiteralExp;
import org.eclipse.ocl.pivot.OCLExpression;
import org.eclipse.ocl.pivot.OperationCallExp;
import org.eclipse.ocl.pivot.PropertyCallExp;
import org.eclipse.ocl.pivot.StringLiteralExp;

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
      this.handleRootExpression(expressionInOCL.getOwnedBody());
    }
  }

  private void handleRootExpression(OCLExpression oclExpression) {
    if (oclExpression instanceof OperationCallExp) {
      //generateConstraintFromOperationCallExp((OperationCallExp) oclExpression, 1);
    } else if (oclExpression instanceof IteratorExp) {
      handleIteratorExp((IteratorExp) oclExpression);
    } else {
      throw new AssertionError(
          "Unknown type of oclExpression: +" + oclExpression);
    }
  }

  private Constraint generateConstraintFromOperationCallExp(OperationCallExp operationCallExp,
      int node) {

    switch (operationCallExp.getName()) {
      case "implies":
        break;
      case "=":
        return handleTwoSidedOperationCall(operationCallExp.getOwnedSource(),
            operationCallExp.getOwnedArguments().get(0), "=", node);
      case ">=":
        return handleTwoSidedOperationCall(operationCallExp.getOwnedSource(),
            operationCallExp.getOwnedArguments().get(0), ">=", node);
      default:
        throw new AssertionError(String
            .format("Unknown Operation name %s: %s", operationCallExp.getName(), operationCallExp));

    }

    System.out.println(operationCallExp);
    return null;
  }

  private void handleIteratorExp(IteratorExp iteratorExp) {

    switch (iteratorExp.getReferredIteration().getName()) {
      case "forAll":
        postForAllConstraint(iteratorExp);
        break;
      case "exists":
        postExistsConstraint(iteratorExp);
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

  private Constraint handleTwoSidedOperationCall(OCLExpression left, OCLExpression right,
      String operator, int node) {
    //currently we assume that the left side is always a property call expression
    checkState(left instanceof PropertyCallExp);

    //variable representing the left side
    Variable leftVariable = getVariableForEAttribute(getAttributeForPropertyCallExpression(
        (PropertyCallExp) left), node);

    Variable rightVariable;
    if (right instanceof PropertyCallExp) {
      rightVariable = getVariableForEAttribute(getAttributeForPropertyCallExpression(
          (PropertyCallExp) right), node);
    } else if (right instanceof LiteralExp) {
      rightVariable = getVariableForLiteralExpression(getAttributeForPropertyCallExpression(
          (PropertyCallExp) left), (LiteralExp) right, node);
    } else {
      throw new AssertionError("Can not handle right sided expression " + right);
    }

    return modelGenerationContext.getModel()
        .arithm((IntVar) leftVariable, operator, (IntVar) rightVariable);
  }

  private EAttribute getAttributeForPropertyCallExpression(PropertyCallExp propertyCallExp) {
    return (EAttribute) propertyCallExp.getReferredProperty().getESObject();
  }

  private Variable getVariableForEAttribute(EAttribute eAttribute, int node) {
    return modelGenerationContext.getVariableStore().getVariables(node).get(eAttribute);
  }

  private Variable getVariableForLiteralExpression(EAttribute context,
      LiteralExp literalExp, int node) {
    if (literalExp instanceof EnumLiteralExp) {
      return getVariableForEnumLiteralExpression(context, (EnumLiteralExp) literalExp, node);
    } else if (literalExp instanceof StringLiteralExp) {
      return getVariableForStringLiteralExpression(context, (StringLiteralExp) literalExp, node);
    } else if (literalExp instanceof NumericLiteralExp) {
      return getVariableForNumericLiteralExpression(context, (NumericLiteralExp) literalExp, node);
    } else {
      throw new AssertionError("Can not handle literal expression " + literalExp);
    }
  }

  private Variable getVariableForStringLiteralExpression(EAttribute context,
      StringLiteralExp stringLiteralExp, int node) {
    int value = modelGenerationContext.mapValue(stringLiteralExp.getStringSymbol(), context);
    return generateConstant(context, node, value);
  }

  private Variable getVariableForNumericLiteralExpression(EAttribute context,
      NumericLiteralExp numericLiteralExp, int node) {
    if (numericLiteralExp instanceof IntegerLiteralExp) {
      return generateConstant(context, node,
          ((IntegerLiteralExp) numericLiteralExp).getIntegerSymbol().intValue());
    } else {
      throw new AssertionError("Can not handle numeric literal expression " + numericLiteralExp);
    }
  }

  private Variable generateConstant(EAttribute context, int node, int value) {
    return modelGenerationContext.getModel().intVar(String
        .format("Constant - %s.%s - %s", context.getEContainingClass().getName(), context.getName(),
            node), value);
  }

  private Variable getVariableForEnumLiteralExpression(EAttribute context,
      EnumLiteralExp enumLiteralExp, int node) {
    int value = modelGenerationContext
        .mapValue(enumLiteralExp.getReferredLiteral().getEnumerator(), context);
    return generateConstant(context, node, value);
  }

  private void postIsUniqueConstraint(IteratorExp iteratorExp) {

    IntVar[] intVars = new IntVar[modelGenerationContext.nodeSize()];

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
      PropertyCallExp propertyCallExp = (PropertyCallExp) iteratorExp.getOwnedBody();

      intVars[node - 1] = (IntVar) getVariableForEAttribute(getAttributeForPropertyCallExpression(
          propertyCallExp), node);
    }
    modelGenerationContext.getModel().allDifferent(intVars).post();
  }

  private void postForAllConstraint(IteratorExp iteratorExp) {
    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
      final OCLExpression ownedBody = iteratorExp.getOwnedBody();
      checkState(ownedBody instanceof OperationCallExp);
      final Constraint constraint = generateConstraintFromOperationCallExp(
          (OperationCallExp) ownedBody, node);
      constraint.post();
    }
  }

  private void postExistsConstraint(IteratorExp iteratorExp) {
    Constraint[] constraints = new Constraint[modelGenerationContext.nodeSize()];
    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
      final OCLExpression ownedBody = iteratorExp.getOwnedBody();
      checkState(ownedBody instanceof OperationCallExp);
      final Constraint constraint = generateConstraintFromOperationCallExp(
          (OperationCallExp) ownedBody, node);
      constraints[node - 1] = constraint;
    }
    modelGenerationContext.getModel().or(constraints).post();
  }


}
