package org.cloudiator.matchmaking.domain;

public interface OCLOptimization extends Optimization {

  static OCLOptimization of(Objective objective, String expression) {
    return new OCLOptimizationImpl(objective, expression);
  }

  String expression();

}
