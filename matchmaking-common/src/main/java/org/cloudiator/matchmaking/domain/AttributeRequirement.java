package org.cloudiator.matchmaking.domain;

public interface AttributeRequirement extends Requirement {

  String requirementClass();

  String requirementAttribute();

  RequirementOperator requirementOperator();

  String value();
}
