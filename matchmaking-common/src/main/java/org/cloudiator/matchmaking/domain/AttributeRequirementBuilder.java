package org.cloudiator.matchmaking.domain;

public class AttributeRequirementBuilder {

  private String requirementClass;
  private String requirementAttribute;
  private RequirementOperator requirementOperator;
  private String value;

  public static AttributeRequirementBuilder newBuilder() {
    return new AttributeRequirementBuilder();
  }

  public AttributeRequirementBuilder requirementClass(String requirementClass) {
    this.requirementClass = requirementClass;
    return this;
  }

  public AttributeRequirementBuilder requirementAttribute(String requirementAttribute) {
    this.requirementAttribute = requirementAttribute;
    return this;
  }

  public AttributeRequirementBuilder requirementOperator(
      RequirementOperator requirementOperator) {
    this.requirementOperator = requirementOperator;
    return this;
  }

  public AttributeRequirementBuilder value(String value) {
    this.value = value;
    return this;
  }

  public AttributeRequirement build() {
    return new AttributeRequirementImpl(requirementClass, requirementAttribute, requirementOperator,
        value);
  }
}
