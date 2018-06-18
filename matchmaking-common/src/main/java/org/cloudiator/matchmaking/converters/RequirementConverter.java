package org.cloudiator.matchmaking.converters;

import com.google.common.collect.Sets;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import java.util.Set;
import org.cloudiator.matchmaking.domain.AttributeRequirement;
import org.cloudiator.matchmaking.domain.AttributeRequirementBuilder;
import org.cloudiator.matchmaking.domain.IdRequirement;
import org.cloudiator.matchmaking.domain.OclRequirement;
import org.cloudiator.matchmaking.domain.Requirement;
import org.cloudiator.matchmaking.domain.RequirementOperator;
import org.cloudiator.messages.NodeEntities.NodeRequirements;
import org.cloudiator.messages.entities.CommonEntities;

public class RequirementConverter implements OneWayConverter<NodeRequirements, Set<Requirement>> {

  @Override
  public Set<Requirement> apply(NodeRequirements nodeRequirements) {

    Set<Requirement> requirements = Sets
        .newHashSetWithExpectedSize(nodeRequirements.getRequirementsCount());

    for (CommonEntities.Requirement requirement : nodeRequirements.getRequirementsList()) {
      switch (requirement.getRequirementCase()) {
        case IDREQUIREMENT:
          requirements.add(convertIdRequirement(requirement.getIdRequirement()));
          break;
        case OCLREQUIREMENT:
          requirements.add(convertOclRequirement(requirement.getOclRequirement()));
          break;
        case REQUIREMENT_NOT_SET:
          throw new AssertionError("Requirement type not set.");
        case ATTRIBUTEREQUIREMENT:
          requirements.add(convertAttributeRequirement(requirement.getAttributeRequirement()));
          break;
        default:
          throw new AssertionError(
              String.format("Unknown requirement type %s.", requirement.getRequirementCase()));
      }
    }

    return requirements;
  }


  private AttributeRequirement convertAttributeRequirement(
      CommonEntities.AttributeRequirement attributeRequirement) {
    return AttributeRequirementBuilder.newBuilder()
        .requirementAttribute(attributeRequirement.getRequirementAttribute())
        .requirementClass(attributeRequirement.getRequirementClass()).requirementOperator(
            RequirementOperator.valueOf(attributeRequirement.getRequirementOperator().name()))
        .value(attributeRequirement.getValue())
        .build();
  }

  private IdRequirement convertIdRequirement(CommonEntities.IdRequirement idRequirement) {
    return IdRequirement.of(idRequirement.getHardwareId(), idRequirement.getLocationId(),
        idRequirement.getImageId());
  }

  private OclRequirement convertOclRequirement(CommonEntities.OclRequirement oclRequirement) {
    return OclRequirement.of(oclRequirement.getConstraint());
  }

}
