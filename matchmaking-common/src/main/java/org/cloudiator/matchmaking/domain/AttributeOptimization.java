package org.cloudiator.matchmaking.domain;

public interface AttributeOptimization extends Optimization {

  enum Aggregation {
    AVG,
    SUM
  }

  String objectiveClass();

  String objectiveAttribute();

  Aggregation aggregation();

}
