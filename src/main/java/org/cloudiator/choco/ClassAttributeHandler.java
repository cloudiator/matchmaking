package org.cloudiator.choco;

import static com.google.common.base.Preconditions.checkState;

import cloudiator.CloudiatorPackage.Literals;
import com.google.common.base.Supplier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.chocosolver.solver.variables.IntVar;
import org.cloudiator.EMFUtil;
import org.cloudiator.ocl.OclCsp;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.pivot.CallExp;
import org.eclipse.ocl.pivot.LoopExp;
import org.eclipse.ocl.pivot.OCLExpression;
import org.eclipse.ocl.pivot.PropertyCallExp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassAttributeHandler {

  private final ModelGenerationContext modelGenerationContext;
  private final EMFUtil emfUtil;
  private static final Logger LOGGER = LoggerFactory.getLogger(ClassAttributeHandler.class);
  private final AttributeCacheLoader attributeCacheLoader = new AttributeCacheLoader();
  private RelevantAttributeCache relevantAttributeCache = null;

  private static class RelevantAttributeCache {

    private final Set<EAttribute> cache = new HashSet<>();

    private void store(EAttribute eAttribute) {
      this.cache.add(eAttribute);
    }

    private boolean check(EAttribute eAttribute) {
      return cache.contains(eAttribute);
    }

  }

  private class AttributeCacheLoader {

    private final Set<EAttribute> mandatoryAttributes = new HashSet<EAttribute>() {{
      add((EAttribute) Literals.PRICE.getEStructuralFeature("price"));
    }};

    private void loadAttributeCache(OclCsp oclCsp) {
      checkState(relevantAttributeCache == null, "Attribute cache already loaded");
      relevantAttributeCache = new RelevantAttributeCache();
      mandatoryAttributes.forEach(eAttribute -> relevantAttributeCache.store(eAttribute));
      oclCsp.getConstraints().forEach(
          expressionInOCL -> handleOclExpression(expressionInOCL.getOwnedBody()));
    }

    private void handleOclExpression(OCLExpression oclExpression) {
      if (oclExpression instanceof CallExp) {
        this.handleOclExpression(((CallExp) oclExpression).getOwnedSource());
      }
      if (oclExpression instanceof LoopExp) {
        this.handleOclExpression(((LoopExp) oclExpression).getOwnedBody());
      }
      if (oclExpression instanceof PropertyCallExp) {
        final EObject esObject = ((PropertyCallExp) oclExpression).getReferredProperty()
            .getESObject();
        if (esObject instanceof EAttribute) {
          EAttribute eAttribute = (EAttribute) esObject;
          relevantAttributeCache.store(eAttribute);
        }
      }
    }


  }

  ClassAttributeHandler(ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
    this.emfUtil = EMFUtil.of(modelGenerationContext.getCloudiatorModel());
    attributeCacheLoader.loadAttributeCache(modelGenerationContext.getOclCsp());
  }

  private boolean isRelevant(EAttribute eAttribute) {
    return eAttribute.isID() || relevantAttributeCache.check(eAttribute);
  }


  private void generate() {
    handleClass(Literals.CLOUD);
    handleClass(Literals.API);
    handleClass(Literals.CLOUD_CREDENTIAL);

    handleClass(Literals.HARDWARE);

    handleClass(Literals.IMAGE);
    handleClass(Literals.OPERATING_SYSTEM);

    handleClass(Literals.LOCATION);
    handleClass(Literals.GEO_LOCATION);

    handleClass(Literals.PRICE);
  }

  private void handleClass(EClass eClass) {
    handleAttributesOfClass(eClass);
    if (eClass.getEIDAttribute() == null) {
      addArtificialId(eClass);
    }
  }

  private void addArtificialId(EClass eClass) {

    final Collection<EObject> allObjectsOfClass = emfUtil.getAllObjectsOfClass(eClass);

    int[] domain;
    if (allObjectsOfClass.isEmpty()) {
      domain = new int[]{modelGenerationContext.getOidGenerator().generateIdFor(eClass, null)};
    } else {
      domain = allObjectsOfClass.stream().mapToInt(
          eObject -> modelGenerationContext.getOidGenerator().generateIdFor(eClass, eObject))
          .toArray();
    }

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
      String name = String.format("%s.OID - %s", eClass.getName(), node);
      final IntVar intVar = modelGenerationContext.getModel().intVar(name, domain);
      modelGenerationContext.getVariableStore()
          .storeIdVariable(node, eClass, intVar);

      LOGGER.debug(String
          .format("Generating variable %s to represent artificial OID for class %s for node %s.",
              intVar, eClass.getName(), node));

    }


  }

  private void handleAttributesOfClass(EClass eClass) {
    for (EAttribute eAttribute : eClass.getEAllAttributes()) {
      if (isRelevant(eAttribute)) {
        handleAttribute(eAttribute);
      }
      LOGGER.debug(String
          .format("Skipping attribute %s as it is not relevant for the problem.", eAttribute));
    }
  }

  private void handleAttribute(EAttribute eAttribute) {

    //create a new variable for every node
    for (int i = 1; i <= modelGenerationContext.nodeSize(); i++) {

      final Set<Integer> domain = deriveDomain(eAttribute);

      //how to handle variables with empty domains?
      //todo
      if (domain.isEmpty()) {
        throw new IllegalStateException(
            String.format("Domain of variable %s is empty.", generateVariableName(eAttribute, i)));
      }

      final IntVar intVar;
      if (domain.size() == 1) {
        intVar = modelGenerationContext.getModel()
            .intVar(generateVariableName(eAttribute, i),
                domain.stream().findFirst().orElseThrow(
                    (Supplier<IllegalStateException>) () -> new IllegalStateException(String.format(
                        "Expected domain of attribute %s to contain only one value. Got %s values.",
                        eAttribute, domain.size()))));
      } else {
        intVar = modelGenerationContext.getModel()
            .intVar(generateVariableName(eAttribute, i),
                domain.stream().mapToInt(Integer::intValue).toArray());
      }

      modelGenerationContext.getVariableStore().storeVariable(i, eAttribute, intVar);

      LOGGER.debug(String
          .format("Adding new variable %s to represent attribute %s of class %s of node %s.",
              intVar, eAttribute.getName(), eAttribute.getContainerClass().getName(), i));
    }

  }

  private String generateVariableName(EAttribute eAttribute, int nodeSize) {
    return String
        .format("%s.%s - %s", eAttribute.getEContainingClass().getName(), eAttribute.getName(),
            nodeSize);
  }

  private Set<Integer> deriveDomain(EAttribute eAttribute) {

    Collection<Object> attributeValues = emfUtil.getValuesOfAttribute(eAttribute);
    Set<Integer> domain = new HashSet<>();

    if (attributeValues.isEmpty()) {
      domain.add(modelGenerationContext.mapValue(null, eAttribute));
    } else {
      for (Object o : attributeValues) {
        domain.add(modelGenerationContext.mapValue(o, eAttribute));
      }
    }
    return domain;
  }

  public static class ClassAttributeContextVisitor implements ModelGenerationContextVisitor {

    @Override
    public void visit(ModelGenerationContext modelGenerationContext) {
      new ClassAttributeHandler(modelGenerationContext).generate();
    }
  }


}
