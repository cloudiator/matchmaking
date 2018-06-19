package org.cloudiator.matchmaking.domain;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class OptimizationImpl implements Optimization {

  private final Objective objective;

  protected OptimizationImpl(Objective objective) {
    checkNotNull(objective, "objective is null");
    this.objective = objective;
  }

  @Override
  public Objective objective() {
    return objective;
  }
}
