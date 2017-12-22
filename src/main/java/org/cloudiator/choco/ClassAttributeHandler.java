package org.cloudiator.choco;

import cloudiator.CloudiatorPackage.Literals;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.chocosolver.solver.variables.IntVar;
import org.cloudiator.EMFUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;

public class ClassAttributeHandler {

  private final ModelGenerationContext modelGenerationContext;
  private final EMFUtil emfUtil;

  ClassAttributeHandler(ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
    this.emfUtil = EMFUtil.of(modelGenerationContext.getCloudiatorModel());
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

    final int[] domain = emfUtil.getAllObjectsOfClass(eClass).stream().mapToInt(
        eObject -> modelGenerationContext.getOidGenerator().generateIdFor(eClass, eObject))
        .toArray();

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
      String name = String.format("%s.OID - %s", eClass.getName(), node);
      final IntVar intVar = modelGenerationContext.getModel().intVar(name, domain);
      modelGenerationContext.getVariableStore()
          .storeIdVariable(node, eClass, intVar);

      System.out.println(String.format("Generating variable %s to represent artificial OID for class %s for node %s.",intVar,eClass.getName(),node));

    }



  }

  private void handleAttributesOfClass(EClass eClass) {
    for (EAttribute eAttribute : eClass.getEAllAttributes()) {
      handleAttribute(eAttribute);
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

      final IntVar intVar = modelGenerationContext.getModel()
          .intVar(generateVariableName(eAttribute, i),
              domain.stream().mapToInt(Integer::intValue).toArray());

      modelGenerationContext.getVariableStore().storeVariable(i, eAttribute, intVar);

      System.out.println(String
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

    for (Object o : attributeValues) {
      domain.add(modelGenerationContext.mapValue(o, eAttribute));

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
