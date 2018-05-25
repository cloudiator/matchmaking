package org.cloudiator.matchmaking.domain;

public class AttributeRequirementImpl implements AttributeRequirement {

  private final String requirementClass;
  private final String requirementAttribute;
  private final RequirementOperator requirementOperator;
  private final String value;

  AttributeRequirementImpl(String requirementClass, String requirementAttribute,
      RequirementOperator requirementOperator, String value) {
    this.requirementClass = requirementClass;
    this.requirementAttribute = requirementAttribute;
    this.requirementOperator = requirementOperator;
    this.value = value;
  }

  @Override
  public String requirementClass() {
    return requirementClass;
  }

  @Override
  public String requirementAttribute() {
    return requirementAttribute;
  }

  @Override
  public RequirementOperator requirementOperator() {
    return requirementOperator;
  }

  @Override
  public String value() {
    return this.value;
  }
}
