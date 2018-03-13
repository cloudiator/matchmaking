package org.cloudiator.domain;

public enum RequirementOperator {

  EQ("="),
  LEQ("<="),
  GEQ(">="),
  GT(">"),
  LT("<");

  private final String operator;

  RequirementOperator(String operator) {
    this.operator = operator;
  }

  public String operator() {
    return operator;
  }

}
