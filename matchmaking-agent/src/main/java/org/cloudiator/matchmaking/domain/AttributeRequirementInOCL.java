package org.cloudiator.matchmaking.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import cloudiator.CloudiatorPackage;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.emf.ecore.EEnum;

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
    if (NumberUtils.isCreatable(value) || isEnum(value)) {
      //its a number or enum, return
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

  public static boolean isEnum(String value) {
    //try to split it at "::"
    String[] split = value.split("::");
    if (split.length != 2) {
      return false;
    }

    String enumClass = split[0];
    //check if class is enum
    try {
      EEnum eEnum = (EEnum) CloudiatorPackage.eINSTANCE.getEClassifier(enumClass);
    } catch (Exception ignored) {
      return false;
    }

    return true;
  }
}
