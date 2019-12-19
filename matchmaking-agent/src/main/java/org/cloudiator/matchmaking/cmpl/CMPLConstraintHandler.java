package org.cloudiator.matchmaking.cmpl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.primitives.Ints;
import de.uniulm.omi.cloudiator.sword.domain.AttributeQuota.Attribute;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.cloudiator.matchmaking.EMFUtil;
import org.cloudiator.matchmaking.choco.ChocoHelper;
import org.cloudiator.matchmaking.choco.DoubleMapper;
import org.cloudiator.matchmaking.choco.ModelGenerationContext;
import org.cloudiator.matchmaking.choco.ModelGenerationContextVisitor;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.ocl.pivot.AssociationClassCallExp;
import org.eclipse.ocl.pivot.BooleanLiteralExp;
import org.eclipse.ocl.pivot.CallExp;
import org.eclipse.ocl.pivot.CollectionItem;
import org.eclipse.ocl.pivot.CollectionLiteralExp;
import org.eclipse.ocl.pivot.CollectionLiteralPart;
import org.eclipse.ocl.pivot.EnumLiteralExp;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.FeatureCallExp;
import org.eclipse.ocl.pivot.IfExp;
import org.eclipse.ocl.pivot.IntegerLiteralExp;
import org.eclipse.ocl.pivot.InvalidLiteralExp;
import org.eclipse.ocl.pivot.IterateExp;
import org.eclipse.ocl.pivot.Iteration;
import org.eclipse.ocl.pivot.IteratorExp;
import org.eclipse.ocl.pivot.LetExp;
import org.eclipse.ocl.pivot.LiteralExp;
import org.eclipse.ocl.pivot.LoopExp;
import org.eclipse.ocl.pivot.MessageExp;
import org.eclipse.ocl.pivot.NavigationCallExp;
import org.eclipse.ocl.pivot.NullLiteralExp;
import org.eclipse.ocl.pivot.NumericLiteralExp;
import org.eclipse.ocl.pivot.OCLExpression;
import org.eclipse.ocl.pivot.Operation;
import org.eclipse.ocl.pivot.OperationCallExp;
import org.eclipse.ocl.pivot.PrimitiveLiteralExp;
import org.eclipse.ocl.pivot.PropertyCallExp;
import org.eclipse.ocl.pivot.RealLiteralExp;
import org.eclipse.ocl.pivot.StateExp;
import org.eclipse.ocl.pivot.StringLiteralExp;
import org.eclipse.ocl.pivot.TupleLiteralExp;
import org.eclipse.ocl.pivot.TypeExp;
import org.eclipse.ocl.pivot.UnlimitedNaturalLiteralExp;
import org.eclipse.ocl.pivot.UnspecifiedValueExp;
import org.eclipse.ocl.pivot.VariableExp;
import org.eclipse.ocl.pivot.internal.RealLiteralExpImpl;
import org.eclipse.ocl.pivot.internal.values.IntIntegerValueImpl;
import org.eclipse.ocl.pivot.internal.values.RealValueImpl;
import org.eclipse.ocl.pivot.library.AbstractSimpleBinaryOperation;
import org.eclipse.ocl.pivot.values.RealValue;
import org.eclipse.ocl.pivot.values.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMPLConstraintHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      CMPLConstraintHandler.class);
  private static final String CAN_NOT_HANDLE_TYPE_OF_EXPRESSION = "Can not handle type of expression %s: %s.";
  private final ModelGenerationContext modelGenerationContext;
  private final EMFUtil emfUtil;


  private Context context = Context.empty();

  private static void UNSUPPORTED(String reason) {
    throw new UnsupportedOperationException(reason);
  }

  private CMPLConstraintHandler(ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
    this.emfUtil = EMFUtil.of(modelGenerationContext.getCloudiatorModel());
  }

  public static class ConstraintHandlerModelGenerationVisitor implements
      ModelGenerationContextVisitor {

    @Override
    public void visit(ModelGenerationContext modelGenerationContext) {
      new CMPLConstraintHandler(modelGenerationContext).generate();
    }
  }

  public void generate() {
    for (ExpressionInOCL expressionInOCL : modelGenerationContext.getOclCsp()
        .getRelevantConstraints()) {
      checkState(context.isEmpty(), "Expected empty context");
      LOGGER.trace("Handling ExpressionInOCL " + expressionInOCL);
      final ConstraintOrVariable constraintOrVariable = this
          .handleOclExpression(expressionInOCL.getOwnedBody());

      if (!constraintOrVariable.isEmpty()) {
        checkState(constraintOrVariable.isConstraint(),
            "Expected the global parsing to return a constraint.");
        constraintOrVariable.getConstraint().post();
      }
      this.context = Context.empty();
    }
  }


  private ConstraintOrVariable handleOclExpression(OCLExpression oclExpression) {

    if (oclExpression instanceof IfExp) {
      return handleIfExp((IfExp) oclExpression);
    } else if (oclExpression instanceof VariableExp) {
      return handleVariableExp((VariableExp) oclExpression);
    } else if (oclExpression instanceof MessageExp) {
      return handleMessageExp((MessageExp) oclExpression);
    } else if (oclExpression instanceof StateExp) {
      return handleStateExp((StateExp) oclExpression);
    } else if (oclExpression instanceof UnspecifiedValueExp) {
      return handleUnspecifiedValueExp((UnspecifiedValueExp) oclExpression);
    } else if (oclExpression instanceof CallExp) {
      return handleCallExp((CallExp) oclExpression);
    } else if (oclExpression instanceof LetExp) {
      return handleLetExp((LetExp) oclExpression);
    } else if (oclExpression instanceof LiteralExp) {
      return handleLiteralExp((LiteralExp) oclExpression);
    } else if (oclExpression instanceof TypeExp) {
      return handleTypeExp((TypeExp) oclExpression);
    } else {
      throw new AssertionError(String
          .format(CAN_NOT_HANDLE_TYPE_OF_EXPRESSION, oclExpression,
              oclExpression.getClass().getName()));
    }
  }

  private ConstraintOrVariable handleIfExp(IfExp ifExp) {
    UNSUPPORTED("handleIfExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleVariableExp(VariableExp variableExp) {
    UNSUPPORTED("handleVariableExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleMessageExp(MessageExp messageExp) {
    UNSUPPORTED("handleMessageExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleStateExp(StateExp stateExp) {
    UNSUPPORTED("handleStateExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleUnspecifiedValueExp(UnspecifiedValueExp unspecifiedValueExp) {
    UNSUPPORTED("handleUnspecifiedValueExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleCallExp(CallExp callExp) {
    if (callExp instanceof FeatureCallExp) {
      return handleFeatureCallExp((FeatureCallExp) callExp);
    } else if (callExp instanceof LoopExp) {
      return handleLoopExp((LoopExp) callExp);
    } else {
      throw new AssertionError(String
          .format(CAN_NOT_HANDLE_TYPE_OF_EXPRESSION, callExp,
              callExp.getClass().getName()));
    }
  }

  private ConstraintOrVariable handleFeatureCallExp(FeatureCallExp featureCallExp) {

    if (featureCallExp instanceof OperationCallExp) {
      return handleOperationCallExp((OperationCallExp) featureCallExp);
    } else if (featureCallExp instanceof NavigationCallExp) {
      return handleNavigationCallExp((NavigationCallExp) featureCallExp);
    } else {
      throw new AssertionError(String
          .format(CAN_NOT_HANDLE_TYPE_OF_EXPRESSION, featureCallExp,
              featureCallExp.getClass().getName()));
    }
  }

  private ConstraintOrVariable handleOperationCallExp(OperationCallExp operationCallExp) {
    final OCLExpression source = operationCallExp.getOwnedSource();
    final Operation referredOperation = operationCallExp.getReferredOperation();

    switch (referredOperation.getName()) {
      case ">=":
      case "=":
      case ">":
      case "<":
      case "<=":
        return ConstraintOrVariable.fromConstraint(
            handleTwoSidedOperation(source, operationCallExp.getOwnedArguments().get(0),
                referredOperation.getName()));
      case "<>":
        return ConstraintOrVariable.fromConstraint(
            handleTwoSidedOperation(source, operationCallExp.getOwnedArguments().get(0),
                "!="));
      case "implies":
        return handleLogicalOperation(source, operationCallExp.getOwnedArguments().get(0),
            "implies");
      case "sum":
        return handleCollectOperation(operationCallExp, "sum");
      case "size":
        return handleSizeOperation(operationCallExp);
      case "includes":
        return handleIncludesOperation(operationCallExp);
      case "/":
        return handleMathOperation(operationCallExp);
      default:
        throw new AssertionError("Can not handle operation of type:" + referredOperation.getName());
    }
  }

  private Value getValue(OCLExpression oclExpression) {
    switch (oclExpression.getType().getName()) {
      case "Integer":
        IntegerLiteralExp integerLiteralExp = (IntegerLiteralExp) oclExpression;
        return new IntIntegerValueImpl(integerLiteralExp.getIntegerSymbol().intValue());
      case "Real":
        RealLiteralExpImpl realLiteralExp = (RealLiteralExpImpl) oclExpression;
        return new RealValueImpl(realLiteralExp.getRealSymbol().doubleValue());
      default:
        throw new IllegalStateException();
    }
  }

  private ConstraintOrVariable handleMathOperation(OperationCallExp operationCallExp) {

    OCLExpression left = operationCallExp.getOwnedSource();
    OCLExpression right = operationCallExp.getOwnedArguments().get(0);

    final String implementationClass = operationCallExp.getReferredOperation()
        .getImplementationClass();

    try {
      final AbstractSimpleBinaryOperation operation = Class.forName(implementationClass)
          .asSubclass(AbstractSimpleBinaryOperation.class).newInstance();

      RealValue evaluate = (RealValue) operation.evaluate(getValue(left), getValue(right));

      return ConstraintOrVariable
          .fromVariable(
              modelGenerationContext.getModel()
                  .intVar(new DoubleMapper().applyAsInt(evaluate.doubleValue())));

    } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
      throw new IllegalStateException(e);
    }
  }

  private ConstraintOrVariable handleIncludesOperation(OperationCallExp operationCallExp) {

    final OCLExpression ownedSource = operationCallExp.getOwnedSource();
    final OCLExpression argument = operationCallExp.getOwnedArguments().get(0);

    //first, handle the attribute so that the context is set
    final ConstraintOrVariable attribute = handleOclExpression(argument);
    final ConstraintOrVariable set = handleOclExpression(ownedSource);

    checkState(set.isVariable(), "Expected left side (set) to be a variable.");
    checkState(attribute.isVariable(), "Expected right side (included) to be a variable");

    checkState(attribute.getVariable() instanceof IntVar,
        "Expected right side (included) to be a int var");
    checkState(set.getVariable() instanceof SetVar,
        "Expected left side (set) to be a set variable.");

    final Constraint member = modelGenerationContext.getModel()
        .member((IntVar) attribute.getVariable(), (SetVar) set.getVariable());

    return ConstraintOrVariable.fromConstraint(member);
  }

  private ConstraintOrVariable handleSizeOperation(OperationCallExp operationCallExp) {




    final ConstraintOrVariable constraintOrVariable = handleOclExpression(
        operationCallExp.getOwnedSource());

    checkState(constraintOrVariable.isMultipleVariables(),
        "Expected body of size operation to return multiple variables");

    //generate a count for all variables with a limit var
    final IntVar limit = modelGenerationContext.getModel()
        .intVar(operationCallExp.toString(), 0, modelGenerationContext.nodeSize());

    IntVar[] vars = constraintOrVariable.getVariables().stream()
        .map(var -> ((IntVar) var)).toArray(IntVar[]::new);

    modelGenerationContext.getModel().count(1,
        vars, limit).post();

    return ConstraintOrVariable.fromVariable(limit);
  }

  private ConstraintOrVariable handleCollectOperation(OperationCallExp operationCallExp,
      String type) {

    final ConstraintOrVariable constraintOrVariable = handleOclExpression(
        operationCallExp.getOwnedSource());
    checkState(constraintOrVariable.isMultipleVariables(),
        "Expected collect expression to have multiple variables");

    switch (type) {
      case "sum":
        IntVar sumVar = modelGenerationContext.getModel()
            .intVar(operationCallExp.toString(), 0, IntVar.MAX_INT_BOUND);
        //noinspection SuspiciousToArrayCall
        modelGenerationContext.getModel().sum(
            constraintOrVariable.getVariables()
                .toArray(new IntVar[constraintOrVariable.getVariables().size()]), "=", sumVar)
            .post();
        return ConstraintOrVariable.fromVariable(sumVar);
      default:
        throw new AssertionError("Unknown collect operation " + type);
    }
  }

  private ConstraintOrVariable handleLogicalOperation(OCLExpression left, OCLExpression right,
      String operator) {

    ConstraintOrVariable leftConstraint = handleOclExpression(left);
    checkState(leftConstraint.isConstraint(),
        "Logic operation expects left expression to be a constraint.");
    ConstraintOrVariable rightConstraint = handleOclExpression(right);
    checkState(rightConstraint.isConstraint(),
        "Logic operation expects right expression to be a constraint");

    switch (operator) {
      case "implies":
        this.modelGenerationContext.getModel()
            .ifThen(leftConstraint.getConstraint(), rightConstraint.getConstraint());
        return ConstraintOrVariable.empty();
      default:
        throw new AssertionError(
            "handleLogicalOperation does not support operator of type: " + operator);
    }
  }

  private Constraint handleTwoSidedOperation(OCLExpression left, OCLExpression right,
      String operator) {

    ConstraintOrVariable leftVariable = handleOclExpression(left);
    checkState(leftVariable.isVariable(), "left expression is expected to be a variable.");
    ConstraintOrVariable rightVariable = handleOclExpression(right);
    checkState(rightVariable.isVariable(), "right expression is expected to be a variable");

    final Operator chocoOperator = Operator.get(operator);
    checkState(chocoOperator != null, String.format("Operator %s is unknown in choco", operator));

    return modelGenerationContext.getModel()
        .arithm((IntVar) leftVariable.getVariable(), chocoOperator.toString(),
            (IntVar) rightVariable.getVariable());
  }

  private ConstraintOrVariable handleNavigationCallExp(NavigationCallExp navigationCallExp) {
    if (navigationCallExp instanceof PropertyCallExp) {
      return handlePropertyCallExp((PropertyCallExp) navigationCallExp);
    } else if (navigationCallExp instanceof AssociationClassCallExp) {
      return handleAssociationClassCallExp((AssociationClassCallExp) navigationCallExp);
    } else {
      throw new AssertionError(String
          .format(CAN_NOT_HANDLE_TYPE_OF_EXPRESSION, navigationCallExp,
              navigationCallExp.getClass().getName()));
    }
  }

  private ConstraintOrVariable handlePropertyCallExp(PropertyCallExp propertyCallExp) {

    if (propertyCallExp.getReferredProperty().getName().equals("nodes")) {
      checkState(!context.hasNode(), "Expected context to have no nodes");

      Collection<Variable> variables = new HashSet<>();
      for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
        Variable binary_node = modelGenerationContext.getVariableStore()
            .getCustomVariable(node, Attribute.NODES_SIZE.name());
        if (binary_node == null) {
          binary_node = modelGenerationContext.getModel()
              .boolVar(Attribute.NODES_SIZE.name(), true);
          modelGenerationContext.getVariableStore()
              .storeCustomVariable(node, Attribute.NODES_SIZE.name(), binary_node);
        }
        variables.add(binary_node);
      }

      return ConstraintOrVariable.fromVariables(variables);

    }

    final EAttribute attributeForPropertyCallExpression = getAttributeForPropertyCallExpression(
        propertyCallExp);

    //set the context to this attribute
    context.seteAttribute(attributeForPropertyCallExpression);

    if (this.context.hasNode()) {
      final Variable variableForEAttribute = getVariableForEAttribute(
          attributeForPropertyCallExpression, context.getNode());
      checkState(variableForEAttribute != null,
          String.format("Variable for attribute %s of class %s is null",
              attributeForPropertyCallExpression,
              attributeForPropertyCallExpression.getEContainingClass()));
      return ConstraintOrVariable.fromVariable(variableForEAttribute);
    } else {
      final Collection<Variable> variablesForEAttribute = getVariablesForEAttribute(
          attributeForPropertyCallExpression);
      return ConstraintOrVariable.fromVariables(variablesForEAttribute);
    }
  }

  private ConstraintOrVariable handleAssociationClassCallExp(
      AssociationClassCallExp associationClassCallExp) {
    UNSUPPORTED("handleAssociationClassCallExp");
    return ConstraintOrVariable.empty();
  }


  private ConstraintOrVariable handleLoopExp(LoopExp loopExp) {
    if (loopExp instanceof IteratorExp) {
      return handleIteratorExp((IteratorExp) loopExp);
    } else if (loopExp instanceof IterateExp) {
      return handleIterateExp((IterateExp) loopExp);
    } else {
      throw new AssertionError(String
          .format(CAN_NOT_HANDLE_TYPE_OF_EXPRESSION, loopExp,
              loopExp.getClass().getName()));
    }
  }

  private ConstraintOrVariable handleIteratorExp(IteratorExp iteratorExp) {

    final Iteration referredIteration = iteratorExp.getReferredIteration();
    switch (referredIteration.getName()) {
      case "forAll":
        return generateIterationConstraint(iteratorExp, "forAll");
      case "exists":
        return generateIterationConstraint(iteratorExp, "exists");
      case "collect":
        return handleOclExpression(iteratorExp.getOwnedBody());
      case "isUnique":
        return ConstraintOrVariable.fromConstraint(generateIsUniqueConstraint(iteratorExp));
      case "select":
        return generateIterationConstraint(iteratorExp, "select");
      default:
        throw new AssertionError(String.format("Can not handle iteratorExp of type %s.",
            referredIteration.getName()));
    }
  }

  private ConstraintOrVariable handleIterateExp(IterateExp iterateExp) {
    UNSUPPORTED("handleIterateExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleLetExp(LetExp letExp) {
    UNSUPPORTED("handleLetExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleLiteralExp(LiteralExp literalExp) {
    if (literalExp instanceof TupleLiteralExp) {
      return handleTupleLiteralExp((TupleLiteralExp) literalExp);
    } else if (literalExp instanceof EnumLiteralExp) {
      return handleEnumLiteralExp((EnumLiteralExp) literalExp);
    } else if (literalExp instanceof NullLiteralExp) {
      return handleNullLiteralExp((NullLiteralExp) literalExp);
    } else if (literalExp instanceof PrimitiveLiteralExp) {
      return handlePrimitiveLiteralExp((PrimitiveLiteralExp) literalExp);
    } else if (literalExp instanceof CollectionLiteralExp) {
      return handleCollectionLiteralExp((CollectionLiteralExp) literalExp);
    } else if (literalExp instanceof InvalidLiteralExp) {
      return handleInvalidLiteralExp((InvalidLiteralExp) literalExp);
    } else {
      throw new AssertionError(String
          .format(CAN_NOT_HANDLE_TYPE_OF_EXPRESSION, literalExp,
              literalExp.getClass().getName()));
    }
  }

  private ConstraintOrVariable handleTupleLiteralExp(TupleLiteralExp tupleLiteralExp) {
    UNSUPPORTED("handleTupleLiteralExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleEnumLiteralExp(EnumLiteralExp enumLiteralExp) {

    checkState(context.hasAttribute(), "attribute context required for EnumLiteralExp.");

    return ConstraintOrVariable.fromVariable(
        getVariableForEnumLiteralExpression(context.geteAttribute(), enumLiteralExp));
  }

  private ConstraintOrVariable handleNullLiteralExp(NullLiteralExp nullLiteralExp) {
    UNSUPPORTED("handleNullLiteralExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handlePrimitiveLiteralExp(PrimitiveLiteralExp primitiveLiteralExp) {
    if (primitiveLiteralExp instanceof StringLiteralExp) {
      return handleStringLiteralExp((StringLiteralExp) primitiveLiteralExp);
    } else if (primitiveLiteralExp instanceof NumericLiteralExp) {
      return handleNumericLiteralExp((NumericLiteralExp) primitiveLiteralExp);
    } else if (primitiveLiteralExp instanceof BooleanLiteralExp) {
      return handleBooleanLiteralExp((BooleanLiteralExp) primitiveLiteralExp);
    } else {
      throw new AssertionError(String
          .format(CAN_NOT_HANDLE_TYPE_OF_EXPRESSION, primitiveLiteralExp,
              primitiveLiteralExp.getClass().getName()));
    }
  }

  private ConstraintOrVariable handleStringLiteralExp(StringLiteralExp stringLiteralExp) {

    checkState(context.hasAttribute(), "attribute context required for StringLiteralExp.");

    return ConstraintOrVariable.fromVariable(
        getVariableForStringLiteralExpression(context.geteAttribute(), stringLiteralExp));
  }

  private ConstraintOrVariable handleNumericLiteralExp(NumericLiteralExp numericLiteralExp) {
    if (numericLiteralExp instanceof IntegerLiteralExp) {
      return handleIntegerLiteralExp((IntegerLiteralExp) numericLiteralExp);
    } else if (numericLiteralExp instanceof UnlimitedNaturalLiteralExp) {
      return handleUnlimitedNaturalLiteralExp((UnlimitedNaturalLiteralExp) numericLiteralExp);
    } else if (numericLiteralExp instanceof RealLiteralExp) {
      return handleRealLiteralExp((RealLiteralExp) numericLiteralExp);
    } else {
      throw new AssertionError(String
          .format(CAN_NOT_HANDLE_TYPE_OF_EXPRESSION, numericLiteralExp,
              numericLiteralExp.getClass().getName()));
    }
  }

  private ConstraintOrVariable handleIntegerLiteralExp(IntegerLiteralExp integerLiteralExp) {

    EAttribute eAttribute;
    if (context.hasAttribute()) {
      eAttribute = context.geteAttribute();
    } else {
      eAttribute = null;
    }

    return ConstraintOrVariable
        .fromVariable(getVariableForIntegerLiteralExpression(eAttribute, integerLiteralExp));
  }

  private ConstraintOrVariable handleUnlimitedNaturalLiteralExp(
      UnlimitedNaturalLiteralExp unlimitedNaturalLiteralExp) {
    UNSUPPORTED("handleUnlimitedNaturalLiteralExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleRealLiteralExp(RealLiteralExp realLiteralExp) {
    UNSUPPORTED("handleRealLiteralExp");
    return ConstraintOrVariable.empty();
  }


  private ConstraintOrVariable handleBooleanLiteralExp(BooleanLiteralExp booleanLiteralExp) {
    UNSUPPORTED("handleBooleanLiteralExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleCollectionLiteralExp(
      CollectionLiteralExp collectionLiteralExp) {

    Set<IntVar> intVars = new HashSet<>();

    for (CollectionLiteralPart collectionLiteralPart : collectionLiteralExp.getOwnedParts()) {
      checkState(collectionLiteralPart instanceof CollectionItem,
          "Expected collectionLiteralPart to be a CollectionItem but was " + collectionLiteralPart
              .getClass().getName());
      CollectionItem collectionItem = (CollectionItem) collectionLiteralPart;
      final OCLExpression ownedItem = collectionItem.getOwnedItem();
      checkState(ownedItem instanceof LiteralExp,
          "Expected owned item to be a literal exp but was" + ownedItem.getClass().getName());
      final ConstraintOrVariable constraintOrVariable = handleLiteralExp((LiteralExp) ownedItem);
      checkState(constraintOrVariable.isVariable(),
          "Expected LiteralExp to be represented as a variable.");

      intVars.add((IntVar) constraintOrVariable.getVariable());
    }

    return ConstraintOrVariable.fromVariable(modelGenerationContext.getModel()
        .setVar(Ints.toArray(ChocoHelper.getMergedDomainOfVariables(intVars))));
  }

  private ConstraintOrVariable handleInvalidLiteralExp(InvalidLiteralExp invalidLiteralExp) {
    UNSUPPORTED("handleInvalidLiteralExp");
    return ConstraintOrVariable.empty();
  }

  private ConstraintOrVariable handleTypeExp(TypeExp typeExp) {
    UNSUPPORTED("handleTypeExp");
    return ConstraintOrVariable.empty();
  }

  private EAttribute getAttributeForPropertyCallExpression(PropertyCallExp propertyCallExp) {
    return (EAttribute) propertyCallExp.getReferredProperty().getESObject();
  }

  private Variable getVariableForEAttribute(EAttribute eAttribute, int node) {
    return modelGenerationContext.getVariableStore().getVariables(node).get(eAttribute);
  }

  private Collection<Variable> getVariablesForEAttribute(EAttribute eAttribute) {
    return modelGenerationContext.getVariableStore().getVariables(eAttribute);
  }

  private Variable getVariableForStringLiteralExpression(EAttribute context,
      StringLiteralExp stringLiteralExp) {
    int value = modelGenerationContext.mapValue(stringLiteralExp.getStringSymbol(), context);
    return generateConstant(context, value);
  }

  private Variable getVariableForIntegerLiteralExpression(@Nullable EAttribute context,
      IntegerLiteralExp integerLiteralExp) {
    return generateConstant(context,
        (integerLiteralExp).getIntegerSymbol().intValue());
  }

  private Variable generateConstant(@Nullable EAttribute context, int value) {

    String name;
    if (context != null) {
      name = String
          .format("Constant - %s.%s", context.getEContainingClass().getName(), context.getName());
    } else {
      name = Integer.toString(value);
    }

    return modelGenerationContext.getModel().intVar(name,
        value);
  }

  private Variable getVariableForEnumLiteralExpression(EAttribute context,
      EnumLiteralExp enumLiteralExp) {
    int value = modelGenerationContext
        .mapValue(enumLiteralExp.getReferredLiteral().getEnumerator(), context);
    return generateConstant(context, value);
  }

  private Constraint generateIsUniqueConstraint(IteratorExp iteratorExp) {

    IntVar[] intVars = new IntVar[modelGenerationContext.nodeSize()];

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
      this.context.setNode(node);
      final ConstraintOrVariable constraintOrVariable = handleOclExpression(
          iteratorExp.getOwnedBody());
      checkState(constraintOrVariable.isVariable(),
          "Expected handling of body to return a variable.");

      intVars[node - 1] = (IntVar) constraintOrVariable.getVariable();
    }
    return modelGenerationContext.getModel().allDifferent(intVars);
  }

  private ConstraintOrVariable generateIterationConstraint(IteratorExp iteratorExp, String type) {
    List<Constraint> constraints = new ArrayList<>();
    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
      this.context.setNode(node);
      final OCLExpression ownedBody = iteratorExp.getOwnedBody();
      final ConstraintOrVariable constraintOrVariable = handleOclExpression(ownedBody);

      switch (type) {
        case "forAll":
          checkState(constraintOrVariable.isEmpty() || constraintOrVariable.isConstraint(),
              "expected body to generate a constraint or be empty");
          break;
        case "exists":
        case "select":
          checkState(constraintOrVariable.isConstraint(),
              "expected body to generate a constraint");
          break;
        default:
          throw new AssertionError(String.format("Type %s is not known.", type));
      }
      if (!constraintOrVariable.isEmpty()) {
        constraints.add(constraintOrVariable.getConstraint());
      }

    }

    if (constraints.isEmpty()) {
      return ConstraintOrVariable.empty();
    }

    @SuppressWarnings("SuspiciousToArrayCall") final Constraint[] array = constraints
        .toArray(new Constraint[constraints.size()]);

    switch (type) {
      case "forAll":
        final ConstraintOrVariable forAllConstraint = ConstraintOrVariable
            .fromConstraint(modelGenerationContext.getModel().and(array));
        return forAllConstraint;
      case "exists":
        final ConstraintOrVariable existsConstraint = ConstraintOrVariable
            .fromConstraint(modelGenerationContext.getModel().or(array));
        return existsConstraint;
      case "select":
        int i = 1;
        Collection<BoolVar> boolVars = new HashSet<>();
        for (Constraint constraint : constraints) {
          final BoolVar boolVar = modelGenerationContext.getModel()
              .boolVar("select for node " + i);
          constraint.reifyWith(boolVar);
          i++;
          boolVars.add(boolVar);
        }
        return ConstraintOrVariable.fromVariables(boolVars);

      default:
        throw new AssertionError(String.format("Type %s is not known.", type));
    }
  }

  private static class Context {

    @Nullable
    private Integer node;
    @Nullable
    private EAttribute eAttribute;


    private Context(@Nullable Integer node, @Nullable EAttribute eAttribute) {
      this.node = node;
      this.eAttribute = eAttribute;
    }

    public static Context fromNode(int node) {
      return new Context(node, null);
    }

    public static Context empty() {
      return new Context(null, null);
    }

    public boolean isEmpty() {
      return !hasNode() && !hasAttribute();
    }

    public void setNode(int node) {
      this.node = node;
    }

    public void seteAttribute(EAttribute eAttribute) {
      this.eAttribute = eAttribute;
    }

    public boolean hasNode() {
      return node != null;
    }

    public boolean hasAttribute() {
      return eAttribute != null;
    }

    public Integer getNode() {
      checkState(hasNode(), "node not set");
      return node;
    }

    public EAttribute geteAttribute() {
      checkState(hasAttribute(), "attribute not set");
      return eAttribute;
    }
  }

  private static class ConstraintOrVariable {

    @Nullable
    private final Constraint constraint;
    @Nullable
    private final Variable variable;
    private final Collection<? extends Variable> variables;

    private ConstraintOrVariable(@Nullable Constraint constraint, @Nullable Variable variable,
        Collection<? extends Variable> variables) {
      this.constraint = constraint;
      this.variable = variable;
      this.variables = variables;
    }

    public static ConstraintOrVariable empty() {
      return new ConstraintOrVariable(null, null, Collections.emptyList());
    }

    public static ConstraintOrVariable fromConstraint(Constraint constraint) {
      checkNotNull(constraint, "constraint is null");
      return new ConstraintOrVariable(constraint, null, Collections.emptyList());
    }

    public static ConstraintOrVariable fromVariable(Variable variable) {
      checkNotNull(variable, "variable is null");
      return new ConstraintOrVariable(null, variable, Collections.emptyList());
    }

    public static ConstraintOrVariable fromVariables(Collection<? extends Variable> variables) {
      checkArgument(!variables.isEmpty(), "variables is empty");
      return new ConstraintOrVariable(null, null, variables);
    }

    public boolean isConstraint() {
      return constraint != null;
    }

    public boolean isVariable() {
      return variable != null;
    }

    public boolean isMultipleVariables() {
      return !variables.isEmpty();
    }

    public boolean isEmpty() {
      return !isConstraint() && !isVariable() && !isMultipleVariables();
    }

    public Constraint getConstraint() {
      checkState(isConstraint(), "is not constraint");
      return constraint;
    }

    public Variable getVariable() {
      checkState(isVariable(), "is not variable");
      return variable;
    }

    public Collection<? extends Variable> getVariables() {
      checkState(isMultipleVariables(), "is not multiple variables");
      return variables;
    }
  }


}
