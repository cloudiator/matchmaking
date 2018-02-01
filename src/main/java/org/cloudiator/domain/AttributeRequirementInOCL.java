package org.cloudiator.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang3.math.NumberUtils;

public class AttributeRequirementInOCL implements AttributeRequirement, RepresentableAsOCL {

  private final static String OCL_TEMPLATE = "nodes->forAll(n | n.%s.%s %s %s)";

  private final AttributeRequirement attributeRequirement;

  AttributeRequirementInOCL(AttributeRequirement attributeRequirement) {
    checkNotNull(attributeRequirement, "attributeRequirementImpl is null");
    this.attributeRequirement = attributeRequirement;
  }

  @Override
  public Set<String> getOCLConstraints() {
    return Collections
        .singleton(String.format(OCL_TEMPLATE, attributeRequirement.requirementClass(),
            attributeRequirement.requirementAttribute(),
            attributeRequirement.requirementOperator().operator(),
            handleValue(attributeRequirement.value())));
  }

  private String handleValue(String value) {
    if (NumberUtils.isCreatable(value)) {
      //its a number, return
      return value;
    } else {
      //is a string, wrap
      return "\'" + value + "\'";
    }
  }

  @Override
  public String requirementClass() {
    return attributeRequirement.requirementClass();
  }

  @Override
  public String requirementAttribute() {
    return attributeRequirement.requirementAttribute();
  }

  @Override
  public RequirementOperator requirementOperator() {
    return attributeRequirement.requirementOperator();
  }

  @Override
  public String value() {
    return attributeRequirement.value();
  }
}
