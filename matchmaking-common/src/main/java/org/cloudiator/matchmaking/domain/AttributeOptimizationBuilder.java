package org.cloudiator.matchmaking.domain;

import org.cloudiator.matchmaking.domain.AttributeOptimization.Aggregation;

public class AttributeOptimizationBuilder {

  private Objective objective;
  private String objectiveClass;
  private String objectiveAttribute;
  private Aggregation aggregation;


  AttributeOptimizationBuilder() {

  }

  public static AttributeOptimizationBuilder newBuilder() {
    return new AttributeOptimizationBuilder();
  }

  public AttributeOptimizationBuilder objective(Objective objective) {
    this.objective = objective;
    return this;
  }

  public AttributeOptimizationBuilder objectiveClass(String objectiveClass) {
    this.objectiveClass = objectiveClass;
    return this;
  }

  public AttributeOptimizationBuilder objectiveAttribute(String objectiveAttribute) {
    this.objectiveAttribute = objectiveAttribute;
    return this;
  }

  public AttributeOptimizationBuilder aggregation(Aggregation aggregation) {
    this.aggregation = aggregation;
    return this;
  }

  public AttributeOptimization build() {
    return new AttributeOptimizationImpl(objective, objectiveClass, objectiveAttribute,
        aggregation);
  }


}
