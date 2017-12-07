package org.cloudiator.domain;

public interface AttributeRequirement extends Requirement {

  String requirementClass();

  String requirementAttribute();

  RequirementOperator requirementOperator();

  String value();
}
