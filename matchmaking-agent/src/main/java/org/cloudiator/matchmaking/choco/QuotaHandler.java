package org.cloudiator.matchmaking.choco;

import static com.google.common.base.Preconditions.checkNotNull;

import cloudiator.CloudiatorPackage.Literals;
import cloudiator.Location;
import de.uniulm.omi.cloudiator.sword.domain.AttributeQuota;
import de.uniulm.omi.cloudiator.sword.domain.AttributeQuota.Attribute;
import de.uniulm.omi.cloudiator.sword.domain.OfferQuota;
import de.uniulm.omi.cloudiator.sword.domain.OfferQuota.OfferType;
import de.uniulm.omi.cloudiator.sword.domain.Quota;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.cloudiator.matchmaking.LocationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuotaHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(QuotaHandler.class);
  private final ModelGenerationContext modelGenerationContext;

  public QuotaHandler(
      ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
  }

  public void handle() {

    for (Quota quota : modelGenerationContext.getOclCsp().getQuotaSet().quotaSet()) {

      //todo: we need to handle the case where the location id references the cloud
      if (!quota.locationId().isPresent()) {
        LOGGER.trace(String.format("Ignoring quota %s as it does not reference a location", quota));
        continue;
      }

      LOGGER.trace(String
          .format("Ignoring quota %s as the location %s is not part of the model.", quota,
              quota.locationId().get()));

      if (!checkQuota(quota)) {
        LOGGER.trace("Ignoring quota " + quota);
        continue;
      }

      if (quota instanceof AttributeQuota) {
        handleAttributeQuota((AttributeQuota) quota);
      } else if (quota instanceof OfferQuota) {
        handleOfferQuota((OfferQuota) quota);
      }
    }

  }

  private boolean checkQuota(Quota quota) {
    final Optional<Location> location = LocationUtil
        .findLocation(quota.locationId().get(), modelGenerationContext.getCloudiatorModel());

    if (!location.isPresent()) {
      return false;
    }

    if (quota instanceof AttributeQuota) {
      return checkAttributeQuota((AttributeQuota) quota);
    } else if (quota instanceof OfferQuota) {
      return checkOfferQuota((OfferQuota) quota);
    } else {
      throw new AssertionError("Unknown quota type " + quota.getClass().getName());
    }
  }

  private boolean checkAttributeQuota(AttributeQuota attributeQuota) {
    return true;
  }

  private boolean checkOfferQuota(OfferQuota offerQuota) {
    switch (offerQuota.type()) {
      case HARDWARE:
        return modelGenerationContext.getCloudiatorModel().getClouds().stream()
            .flatMap(c -> c.getHardwareList().stream())
            .anyMatch(h -> h.getId().equals(offerQuota.id()));
      default:
        throw new AssertionError("Unknown offer type " + offerQuota.type());
    }
  }

  private void handleOfferQuota(OfferQuota offerQuota) {

    IntVar[] vars = new IntVar[modelGenerationContext.nodeSize()];

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {

      final String name = String
          .format("node_offerQuota_%s_%s_%s", node, offerQuota.id(), offerQuota.locationId().get());

      final IntVar nodeUsesHardwareAndLocation = modelGenerationContext.getModel().boolVar(name);

      final Location location = LocationUtil
          .findLocation(offerQuota.locationId().get(), modelGenerationContext.getCloudiatorModel())
          .orElseThrow(() -> new IllegalStateException(
              "Could not find location with id " + offerQuota.locationId().get()));

      final Constraint orConstraint = generateSubLocationOrConstraint(location, node);

      //get variable for offer
      final IntVar offerVariable = getVariable(offerQuota.type(), node);

      final Constraint equalConstraint = modelGenerationContext.getModel()
          .arithm(offerVariable, "=", encode(offerQuota.type(), offerQuota.locationId().get()));

      final Constraint orAndEqualConstraint = modelGenerationContext.getModel()
          .and(orConstraint, equalConstraint);

      final Constraint hardwareAndLocationIsUsed = modelGenerationContext.getModel()
          .arithm(nodeUsesHardwareAndLocation, "=", 1);

      //finally, the implies constraint
      modelGenerationContext.getModel().ifThen(orAndEqualConstraint, hardwareAndLocationIsUsed);

      vars[node - 1] = nodeUsesHardwareAndLocation;
    }

    modelGenerationContext.getModel().sum(vars, "<=", offerQuota.remaining().intValue());

  }

  private Constraint generateSubLocationOrConstraint(Location location, int node) {

    //get location id variable
    IntVar locationIdVariable = getLocationIdVariable(node);

    //get all sublocations
    final Set<Location> subLocations = LocationUtil
        .subLocations(location, modelGenerationContext.getCloudiatorModel());
    subLocations.add(location);

    Set<Constraint> constraints = new HashSet<>();
    for (Location forConstraint : subLocations) {
      constraints.add(modelGenerationContext.getModel().arithm(locationIdVariable, "=",
          modelGenerationContext.mapValue(forConstraint.getId(), Literals.LOCATION__ID)));
    }
    return modelGenerationContext.getModel()
        .or(constraints.toArray(new Constraint[0]));
  }

  private void handleAttributeQuota(AttributeQuota attributeQuota) {

    IntVar[] vars = new IntVar[modelGenerationContext.nodeSize()];

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {

      //generate a name
      final String name = String.format("node_%s_%s_%s", node, attributeQuota.attribute().name(),
          attributeQuota.locationId().get());

      //get the variable representing the original attribute
      final IntVar originalVariable = getVariable(attributeQuota.attribute(), node);

      checkNotNull(originalVariable,
          "Could not find variable for attribute " + attributeQuota.attribute());

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

      //generate constrains: if location variable is set to one of the sublocations or itself set the location core variable
      //to this

      Constraint equalsConstraint = modelGenerationContext.getModel()
          .arithm(locationDependentAttribute, "=", originalVariable);

      modelGenerationContext.getModel()
          .ifThen(generateSubLocationOrConstraint(location, node), equalsConstraint);

      vars[node - 1] = locationDependentAttribute;
    }

    //generate sum constraint
    modelGenerationContext.getModel().sum(vars, "<=", attributeQuota.remaining().intValue()).post();
  }

  private IntVar getLocationIdVariable(int node) {
    return (IntVar) modelGenerationContext.getVariableStore().getVariables(node)
        .get(Literals.LOCATION__ID);
  }

  private IntVar getVariable(OfferType offerType, int node) {
    switch (offerType) {
      case HARDWARE:
        return (IntVar) modelGenerationContext.getVariableStore().getIdVariables(node)
            .get(Literals.HARDWARE);
      default:
        throw new AssertionError("Unknown offer type " + offerType);
    }
  }

  private int encode(OfferType offerType, String id) {
    switch (offerType) {
      case HARDWARE:
        return modelGenerationContext.mapValue(id, Literals.HARDWARE__ID);
      default:
        throw new AssertionError("Unknown offer type " + offerType);
    }
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
              .boolVar();
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
