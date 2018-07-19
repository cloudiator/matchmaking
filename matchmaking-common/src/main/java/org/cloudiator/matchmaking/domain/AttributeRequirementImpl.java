package org.cloudiator.matchmaking.domain;

import com.google.common.base.MoreObjects;
import java.util.Objects;

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

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("requirementClass", requirementClass)
        .add("requirementAttribute", requirementAttribute)
        .add("requirementOperator", requirementOperator).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AttributeRequirementImpl that = (AttributeRequirementImpl) o;
    return Objects.equals(requirementClass, that.requirementClass) &&
        Objects.equals(requirementAttribute, that.requirementAttribute) &&
        requirementOperator == that.requirementOperator &&
        Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {

    return Objects.hash(requirementClass, requirementAttribute, requirementOperator, value);
  }
}
