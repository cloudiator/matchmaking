package org.cloudiator.matchmaking.domain;

public enum RequirementOperator {

  EQ("="),
  LEQ("<="),
  GEQ(">="),
  GT(">"),
  LT("<"),
  NEQ("<>"),
  IN("IN");


  private final String operator;

  RequirementOperator(String operator) {
    this.operator = operator;
  }

  public String operator() {
    return operator;
  }

}
