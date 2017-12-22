package org.cloudiator.choco;

import static com.google.common.base.Preconditions.checkState;

import cloudiator.CloudiatorPackage.Literals;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.cloudiator.EMFUtil;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;

public class RelationshipHandler {

  private final ModelGenerationContext modelGenerationContext;
  private final EMFUtil emfUtil;

  RelationshipHandler(ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
    this.emfUtil = EMFUtil.of(modelGenerationContext.getCloudiatorModel());
  }

  public void handle() {

    //handleRelationship(Literals.NODE__CLOUD);
    //handleRelationship(Literals.NODE__IMAGE);
    //handleRelationship(Literals.NODE__HARDWARE);
    //handleRelationship(Literals.NODE__LOCATION);

    handleRelationship(Literals.CLOUD__API);
    handleRelationship(Literals.CLOUD__CLOUDCREDENTIAL);

    handleRelationship(Literals.IMAGE__OPERATING_SYSTEM);
    handleRelationship(Literals.LOCATION__GEO_LOCATION);

    handleRelationship(Literals.PRICE__HARDWARE);
    handleRelationship(Literals.PRICE__IMAGE);
    handleRelationship(Literals.PRICE__LOCATION);
  }

  private void handleRelationship(EReference eReference) {
    if (eReference.isMany()) {
      handleManyRelationship(eReference);
    } else {
      handleOneRelationship(eReference);
    }
  }

  private void handleOneRelationship(EReference eReference) {

    EClass owing = eReference.getEContainingClass();
    EClass remote = eReference.getEReferenceType();

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {

      final IntVar owningIdVariable = (IntVar) modelGenerationContext.getVariableStore()
          .getIdVariables(node).get(owing);
      final IntVar remoteIdVariable = (IntVar) modelGenerationContext.getVariableStore()
          .getIdVariables(node).get(remote);

      checkState(owningIdVariable != null, "Could not find id variable for " + owing.getName());
      checkState(remoteIdVariable != null, "Could not find id variable for " + remote.getName());

      for (EObject object : emfUtil.getAllObjectsOfClass(owing)) {
        final int owningId = valueOfIdAttribute(object);
        final Object remoteObject = object.eGet(eReference, true);
        final int remoteId = valueOfIdAttribute((EObject) remoteObject);

        //owning equal
        final Constraint owningEqual = modelGenerationContext.getModel()
            .arithm(owningIdVariable, "=", owningId);
        final Constraint remoteEqual = modelGenerationContext.getModel()
            .arithm(remoteIdVariable, "=", remoteId);
        modelGenerationContext.getModel().ifThen(owningEqual, remoteEqual);

        System.out.println(String.format(
            "Adding new implies constraint to express relationship between class %s and %s: %s => %s",
            owing.getName(), remote.getName(), owningEqual, remoteEqual));

      }
    }
  }

  private void handleManyRelationship(EReference eReference) {
    System.out.println(eReference.eClass());

    EClass owning = eReference.getEContainingClass();
    EClass remote = eReference.getEReferenceType();

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
      for (EObject object : emfUtil.getAllObjectsOfClass(owning)) {
        int owningId = valueOfIdAttribute(object);

        //we are always guaranteed to get a list as we handle a many relationship
        //noinspection unchecked
        int[] remoteObjects = ((List<Object>) object
            .eGet(eReference, true)).stream().mapToInt(o -> valueOfIdAttribute((EObject) o))
            .toArray();

        Variable variableOwningId = modelGenerationContext.getVariableStore().getIdVariables(node)
            .get(owning);
        Variable variableRemoteId = modelGenerationContext.getVariableStore().getIdVariables(node)
            .get(remote);

        Constraint owningEquals = modelGenerationContext.getModel().arithm(
            (IntVar) variableOwningId, "=", owningId);
        Constraint remoteIn = modelGenerationContext.getModel()
            .member((IntVar) variableRemoteId, remoteObjects);
        modelGenerationContext.getModel().ifThen(owningEquals, remoteIn);
      }
    }
  }

  private int valueOfIdAttribute(EObject eObject) {
    final EAttribute idAttribute = eObject.eClass().getEIDAttribute();
    if (idAttribute != null) {
      return modelGenerationContext.mapValue(eObject.eGet(idAttribute), idAttribute);
    }
    return modelGenerationContext.getOidGenerator().generateIdFor(eObject.eClass(), eObject);
  }

  public static class RelationModelVisitor implements ModelGenerationContextVisitor {

    @Override
    public void visit(ModelGenerationContext modelGenerationContext) {
      new RelationshipHandler(modelGenerationContext).handle();
    }
  }

}
