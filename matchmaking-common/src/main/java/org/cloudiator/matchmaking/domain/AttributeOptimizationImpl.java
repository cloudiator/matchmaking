package org.cloudiator.matchmaking.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class AttributeOptimizationImpl extends OptimizationImpl implements AttributeOptimization {

  private final String objectiveClass;
  private final String objectiveAttribute;
  private final Aggregation aggregation;

  AttributeOptimizationImpl(Objective objective, String objectiveClass, String objectiveAttribute,
      Aggregation aggregation) {

    super(objective);

    checkNotNull(objectiveClass, "objectiveClass is null");
    checkArgument(!objectiveClass.isEmpty(), "objectiveClass is empty");

    this.objectiveClass = objectiveClass;

    checkNotNull(objectiveAttribute, "objectiveAttribute is null");
    checkArgument(!objectiveAttribute.isEmpty(), "objectiveAttribute is empty");

    this.objectiveAttribute = objectiveAttribute;

    checkNotNull(aggregation, "aggregation is null");

    this.aggregation = aggregation;
  }

  @Override
  public String objectiveClass() {
    return objectiveClass;
  }

  @Override
  public String objectiveAttribute() {
    return objectiveAttribute;
  }

  @Override
  public Aggregation aggregation() {
    return aggregation;
  }
}
