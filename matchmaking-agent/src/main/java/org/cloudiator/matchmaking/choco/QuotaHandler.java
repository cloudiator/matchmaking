package org.cloudiator.matchmaking.choco;

import cloudiator.CloudiatorPackage.Literals;
import cloudiator.Location;
import de.uniulm.omi.cloudiator.sword.domain.AttributeQuota;
import de.uniulm.omi.cloudiator.sword.domain.AttributeQuota.Attribute;
import de.uniulm.omi.cloudiator.sword.domain.Quota;
import java.util.HashSet;
import java.util.Set;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.cloudiator.matchmaking.LocationUtil;

public class QuotaHandler {

  private final ModelGenerationContext modelGenerationContext;

  public QuotaHandler(
      ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
  }

  public void handle() {

    for (Quota quota : modelGenerationContext.getOclCsp().getQuotaSet().quotaSet()) {
      if (quota instanceof AttributeQuota) {
        handleAttributeQuota((AttributeQuota) quota);
      }
    }

  }


  private void handleAttributeQuota(AttributeQuota attributeQuota) {

    IntVar[] vars = new IntVar[modelGenerationContext.nodeSize()];

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {

      //generate a name
      final String name = String.format("node_%s_%s_%s", node, attributeQuota.attribute().name(),
          attributeQuota.locationId().get());

      //get the variable representing the original attribute
      final IntVar originalVariable = getVariable(attributeQuota.attribute(), node);

      final Set<Integer> originalDomain = ChocoHelper.getDomainOfVariable(originalVariable);
      originalDomain.add(0);

      //get domain of original variable
      final int[] domainOfVariable = originalDomain.stream()
          .mapToInt(i -> i).toArray();

      //generate a variable
      final IntVar locationDependentAttribute = modelGenerationContext.getModel()
          .intVar(name, domainOfVariable);

      final Location location = LocationUtil.findLocation(attributeQuota.locationId().get(),
          modelGenerationContext.getCloudiatorModel()).get();

      //get all sublocations
      final Set<Location> subLocations = LocationUtil
          .subLocations(location, modelGenerationContext.getCloudiatorModel());
      subLocations.add(location);

      //get location id variable
      IntVar locationIdVariable = getLocationIdVariable(node);

      //generate constrains: if location variable is set to one of the sublocations or itself set the location core variable
      //to this
      Set<Constraint> constraints = new HashSet<>();
      for (Location forConstraint : subLocations) {
        constraints.add(modelGenerationContext.getModel().arithm(locationIdVariable, "=",
            modelGenerationContext.mapValue(forConstraint.getId(), Literals.LOCATION__ID)));
      }
      Constraint orConstraint = modelGenerationContext.getModel()
          .or(constraints.toArray(new Constraint[0]));

      Constraint equalsConstraint = modelGenerationContext.getModel()
          .arithm(locationDependentAttribute, "=", originalVariable);

      modelGenerationContext.getModel().ifThen(orConstraint, equalsConstraint);

      vars[node - 1] = locationDependentAttribute;
    }

    //generate sum constraint
    modelGenerationContext.getModel().sum(vars, "<=", attributeQuota.remaining().intValue()).post();
  }

  private IntVar getLocationIdVariable(int node) {
    return (IntVar) modelGenerationContext.getVariableStore().getVariables(node)
        .get(Literals.LOCATION__ID);
  }

  private IntVar getVariable(Attribute attribute, int node) {

    switch (attribute) {
      case HARDWARE_CORES:
        return (IntVar) modelGenerationContext.getVariableStore().getVariables(node)
            .get(Literals.HARDWARE__CORES);
      case HARDWARE_RAM:
        return (IntVar) modelGenerationContext.getVariableStore().getVariables(node)
            .get(Literals.HARDWARE__RAM);
      case NODES_SIZE:
        Variable nodeSizeVariable = modelGenerationContext.getVariableStore()
            .getCustomVariable(node, Attribute.NODES_SIZE.name());
        if (nodeSizeVariable == null) {
          final IntVar intVar = modelGenerationContext.getModel()
              .intVar(Attribute.NODES_SIZE.name(), 1);
          modelGenerationContext.getVariableStore()
              .storeCustomVariable(node, Attribute.NODES_SIZE.name(), intVar);
          nodeSizeVariable = intVar;
        }
        return (IntVar) nodeSizeVariable;
      default:
        throw new AssertionError("Unsupported Attribute " + attribute);
    }

  }


  public static class QuotaHandlerVisitor implements ModelGenerationContextVisitor {

    @Override
    public void visit(ModelGenerationContext modelGenerationContext) {
      new QuotaHandler(modelGenerationContext).handle();
    }
  }

}
