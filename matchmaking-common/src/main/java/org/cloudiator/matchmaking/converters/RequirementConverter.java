package org.cloudiator.matchmaking.converters;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.matchmaking.domain.AttributeRequirement;
import org.cloudiator.matchmaking.domain.AttributeRequirementBuilder;
import org.cloudiator.matchmaking.domain.IdRequirement;
import org.cloudiator.matchmaking.domain.OclRequirement;
import org.cloudiator.matchmaking.domain.Requirement;
import org.cloudiator.matchmaking.domain.RequirementOperator;
import org.cloudiator.messages.entities.CommonEntities;
import org.cloudiator.messages.entities.CommonEntities.Requirement.Builder;

public class RequirementConverter implements
    TwoWayConverter<CommonEntities.Requirement, Requirement> {

  public static final RequirementConverter INSTANCE = new RequirementConverter();

  private static final IdentifierRequirementConverter IDENTIFIER_REQUIREMENT_CONVERTER = new IdentifierRequirementConverter();
  private static final AttributeRequirementConverter ATTRIBUTE_REQUIREMENT_CONVERTER = new AttributeRequirementConverter();
  private static final OCLRequirementConverter OCL_REQUIREMENT_CONVERTER = new OCLRequirementConverter();

  private RequirementConverter() {

  }

  @Override
  public CommonEntities.Requirement applyBack(Requirement requirement) {
    final Builder builder = CommonEntities.Requirement.newBuilder();
    if (requirement instanceof IdRequirement) {
      builder.setIdRequirement(IDENTIFIER_REQUIREMENT_CONVERTER.applyBack(
          (IdRequirement) requirement));
    } else if (requirement instanceof AttributeRequirement) {
      builder.setAttributeRequirement(ATTRIBUTE_REQUIREMENT_CONVERTER.applyBack(
          (AttributeRequirement) requirement));
    } else if (requirement instanceof OclRequirement) {
      builder.setOclRequirement(OCL_REQUIREMENT_CONVERTER.applyBack((OclRequirement) requirement));
    } else {
      throw new AssertionError("Unknown requirement type " + requirement.getClass().getName());
    }
    return builder.build();
  }

  @Override
  public Requirement apply(CommonEntities.Requirement requirement) {
    switch (requirement.getRequirementCase()) {
      case IDREQUIREMENT:
        return IDENTIFIER_REQUIREMENT_CONVERTER.apply(requirement.getIdRequirement());
      case OCLREQUIREMENT:
        return OCL_REQUIREMENT_CONVERTER.apply(requirement.getOclRequirement());
      case ATTRIBUTEREQUIREMENT:
        return ATTRIBUTE_REQUIREMENT_CONVERTER.apply(requirement.getAttributeRequirement());
      case REQUIREMENT_NOT_SET:
      default:
        throw new AssertionError("Unknown requirement type " + requirement.getRequirementCase());
    }
  }

  private static class IdentifierRequirementConverter implements
      TwoWayConverter<CommonEntities.IdRequirement, IdRequirement> {

    @Override
    public CommonEntities.IdRequirement applyBack(IdRequirement idRequirement) {
      return CommonEntities.IdRequirement.newBuilder().setHardwareId(idRequirement.hardwareId())
          .setImageId(idRequirement.imageId()).setLocationId(idRequirement.locationId()).build();
    }

    @Override
    public IdRequirement apply(CommonEntities.IdRequirement idRequirement) {
      return IdRequirement.of(idRequirement.getHardwareId(), idRequirement.getLocationId(),
          idRequirement.getImageId());
    }
  }

  private static class OCLRequirementConverter implements
      TwoWayConverter<CommonEntities.OclRequirement, OclRequirement> {

    @Override
    public CommonEntities.OclRequirement applyBack(OclRequirement oclRequirement) {
      return CommonEntities.OclRequirement.newBuilder().setConstraint(oclRequirement.constraint())
          .build();
    }

    @Override
    public OclRequirement apply(CommonEntities.OclRequirement oclRequirement) {
      return OclRequirement.of(oclRequirement.getConstraint());
    }
  }

  private static class AttributeRequirementConverter implements
      TwoWayConverter<CommonEntities.AttributeRequirement, AttributeRequirement> {

    @Override
    public CommonEntities.AttributeRequirement applyBack(
        AttributeRequirement attributeRequirement) {
      return CommonEntities.AttributeRequirement.newBuilder()
          .setRequirementAttribute(attributeRequirement.requirementAttribute())
          .setRequirementClass(attributeRequirement.requirementClass()).setRequirementOperator(
              CommonEntities.RequirementOperator
                  .valueOf(attributeRequirement.requirementOperator().name()))
          .setValue(attributeRequirement.value()).build();
    }

    @Override
    public AttributeRequirement apply(CommonEntities.AttributeRequirement attributeRequirement) {
      return AttributeRequirementBuilder.newBuilder()
          .requirementAttribute(attributeRequirement.getRequirementAttribute())
          .requirementClass(attributeRequirement.getRequirementClass()).requirementOperator(
              RequirementOperator.valueOf(attributeRequirement.getRequirementOperator().name()))
          .value(attributeRequirement.getValue())
          .build();
    }
  }
}
