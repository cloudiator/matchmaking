package org.cloudiator.choco;

import cloudiator.CloudiatorPackage.Literals;
import javax.annotation.Nullable;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.cloudiator.EMFUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

public class ClassStructureHandler {

  private final ModelGenerationContext modelGenerationContext;
  private final EMFUtil emfUtil;

  public ClassStructureHandler(ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
    emfUtil = EMFUtil.of(modelGenerationContext.getCloudiatorModel());
  }

  public void handle() {
    handleInternalStructure(Literals.CLOUD);
    handleInternalStructure(Literals.API);
    handleInternalStructure(Literals.CLOUD_CREDENTIAL);

    handleInternalStructure(Literals.HARDWARE);

    handleInternalStructure(Literals.IMAGE);
    handleInternalStructure(Literals.OPERATING_SYSTEM);

    handleInternalStructure(Literals.LOCATION);
    handleInternalStructure(Literals.GEO_LOCATION);

    handleInternalStructure(Literals.PRICE);
  }

  public void handleInternalStructure(EClass eClass) {
    //find out the ID attribute
    @Nullable EAttribute idAttribute = eClass.getEIDAttribute();

    for (EObject eObject : emfUtil.getAllObjectsOfClass(eClass)) {
      for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
        for (EAttribute eAttribute : eClass.getEAllAttributes()) {
          if (!eAttribute.equals(idAttribute)) {
            //id variable
            Variable idVariable = modelGenerationContext.getVariableStore().getIdVariables(node)
                .get(eClass);
            //the attribute variable
            Variable attribute = modelGenerationContext.getVariableStore().getVariables(node)
                .get(eAttribute);
            //id equals constraint

            //value of the id field
            int idValue;
            //are we using an artificial id or not
            if (idAttribute == null) {
              //artificial
              idValue = modelGenerationContext.getOidGenerator().generateIdFor(eClass, eObject);
            } else {
              //not
              idValue = modelGenerationContext.mapValue(eObject.eGet(idAttribute), idAttribute);
            }

            final Constraint idEqual = modelGenerationContext.getModel()
                .arithm((IntVar) idVariable, "=",
                    idValue);
            //attribute equal constraint
            final Constraint attributeEqual = modelGenerationContext.getModel()
                .arithm((IntVar) attribute, "=",
                    modelGenerationContext.mapValue(eObject.eGet(eAttribute), eAttribute));
            //add implies constraint
            modelGenerationContext.getModel().ifThen(idEqual, attributeEqual);

            System.out.println(
                String.format(
                    "Adding new implies constraint to express structure for attribute %s of class %s: %s => %s",
                    eAttribute.getName(), eAttribute.getEContainingClass().getName(), idEqual, attributeEqual));

          }
        }
      }
    }
  }

  public static class ClassStructureHandlerVisitor implements ModelGenerationContextVisitor {

    @Override
    public void visit(ModelGenerationContext modelGenerationContext) {
      new ClassStructureHandler(modelGenerationContext).handle();
    }
  }
}
