package org.cloudiator.matchmaking.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class OCLOptimizationImpl extends OptimizationImpl implements OCLOptimization {

  private final String expression;

  OCLOptimizationImpl(Objective objective, String expression) {
    super(objective);
    checkNotNull(expression, "expression is null");
    checkArgument(!expression.isEmpty(), "expression is empty");
    this.expression = expression;
  }

  @Override
  public String expression() {
    return expression;
  }
}
