package org.cloudiator.matchmaking.choco;

import static com.google.common.base.Preconditions.checkState;

import cloudiator.CloudiatorPackage.Literals;
import javax.annotation.Nullable;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.cloudiator.matchmaking.EMFUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassStructureHandler {

  private final ModelGenerationContext modelGenerationContext;
  private final EMFUtil emfUtil;
  private static final Logger LOGGER = LoggerFactory.getLogger(ClassStructureHandler.class);

  public ClassStructureHandler(ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
    emfUtil = EMFUtil.of(modelGenerationContext.getCloudiatorModel());
  }

  public void handle() {

    handleInternalStructure(Literals.NODE);

    handleInternalStructure(Literals.CLOUD);
    //handleInternalStructure(Literals.API);
    //handleInternalStructure(Literals.CLOUD_CREDENTIAL);

    handleInternalStructure(Literals.HARDWARE);

    handleInternalStructure(Literals.IMAGE);
    //handleInternalStructure(Literals.OPERATING_SYSTEM);

    handleInternalStructure(Literals.LOCATION);
    //handleInternalStructure(Literals.GEO_LOCATION);
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

            //if attribute variable does not exists we don't need it as its irrelevant
            if (attribute == null) {
              continue;
            }

            checkState(idVariable != null, "id variable is null");
            checkState(attribute != null, "attribute variable is null");

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

            LOGGER.trace(
                String.format(
                    "Adding new implies constraint to express structure for attribute %s of class %s: %s => %s",
                    eAttribute.getName(), eAttribute.getEContainingClass().getName(), idEqual,
                    attributeEqual));

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
