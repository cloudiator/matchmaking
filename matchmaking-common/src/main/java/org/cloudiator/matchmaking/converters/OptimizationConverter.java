package org.cloudiator.matchmaking.converters;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.matchmaking.domain.AttributeOptimization;
import org.cloudiator.matchmaking.domain.AttributeOptimization.Aggregation;
import org.cloudiator.matchmaking.domain.AttributeOptimizationBuilder;
import org.cloudiator.matchmaking.domain.OCLOptimization;
import org.cloudiator.matchmaking.domain.Objective;
import org.cloudiator.matchmaking.domain.Optimization;
import org.cloudiator.messages.entities.CommonEntities;
import org.cloudiator.messages.entities.CommonEntities.Optimization.Builder;


public class OptimizationConverter implements
    TwoWayConverter<CommonEntities.Optimization, Optimization> {

  public static final OptimizationConverter INSTANCE = new OptimizationConverter();

  private static final ObjectiveConverter OBJECTIVE_CONVERTER = new ObjectiveConverter();
  private static final AggregationConverter AGGREGATION_CONVERTER = new AggregationConverter();

  private OptimizationConverter() {

  }

  @Override
  public CommonEntities.Optimization applyBack(Optimization optimization) {

    final Builder builder = CommonEntities.Optimization.newBuilder();
    builder.setObjective(OBJECTIVE_CONVERTER.applyBack(optimization.objective()));
    if (optimization instanceof AttributeOptimization) {
      final CommonEntities.AttributeOptimization attributeOptimization = CommonEntities.AttributeOptimization
          .newBuilder()
          .setObjectiveClass(((AttributeOptimization) optimization).objectiveClass())
          .setObjectiveAttribute(((AttributeOptimization) optimization).objectiveAttribute())
          .setAggregation(AGGREGATION_CONVERTER
              .applyBack(((AttributeOptimization) optimization).aggregation())).build();
      builder.setAttributeOptimization(attributeOptimization);
    } else if (optimization instanceof OCLOptimization) {
      builder.setOclOptimization(CommonEntities.OCLOptimization.newBuilder()
          .setExpression(((OCLOptimization) optimization).expression()));
    } else {
      throw new AssertionError("Unknown optimization type " + optimization.getClass().getName());
    }

    return builder.build();
  }

  @Override
  public Optimization apply(CommonEntities.Optimization optimization) {
    switch (optimization.getOptimizationCase()) {
      case OPTIMIZATION_NOT_SET:
        throw new AssertionError("Optimization not set");
      case ATTRIBUTEOPTIMIZATION:
        final CommonEntities.AttributeOptimization attributeOptimization = optimization
            .getAttributeOptimization();
        return AttributeOptimizationBuilder.newBuilder()
            .aggregation(AGGREGATION_CONVERTER.apply(attributeOptimization.getAggregation()))
            .objective(OBJECTIVE_CONVERTER.apply(optimization.getObjective()))
            .objectiveAttribute(attributeOptimization.getObjectiveAttribute())
            .objectiveClass(attributeOptimization.getObjectiveClass())
            .build();
      case OCLOPTIMIZATION:
        return OCLOptimization.of(OBJECTIVE_CONVERTER.apply(optimization.getObjective()),
            optimization.getOclOptimization().getExpression());
      default:
        throw new AssertionError("Unknown optimization type" + optimization.getOptimizationCase());
    }
  }

  private static class ObjectiveConverter implements
      TwoWayConverter<CommonEntities.Objective, Objective> {

    @Override
    public CommonEntities.Objective applyBack(Objective objective) {
      switch (objective) {
        case MAXIMIZE:
          return CommonEntities.Objective.MAXIMIZE;
        case MINIMIZE:
          return CommonEntities.Objective.MINIMIZE;
        default:
          throw new AssertionError("Unknown objective type" + objective);
      }
    }

    @Override
    public Objective apply(CommonEntities.Objective objective) {

      switch (objective) {
        case MINIMIZE:
          return Objective.MINIMIZE;
        case MAXIMIZE:
          return Objective.MAXIMIZE;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown objective type " + objective);
      }
    }
  }

  private static class AggregationConverter implements
      TwoWayConverter<CommonEntities.Aggregation, Aggregation> {

    @Override
    public CommonEntities.Aggregation applyBack(Aggregation aggregation) {
      switch (aggregation) {
        case SUM:
          return CommonEntities.Aggregation.SUM;
        case AVG:
          return CommonEntities.Aggregation.AVG;
        default:
          throw new AssertionError("Unknown aggregation type " + aggregation);
      }
    }

    @Override
    public Aggregation apply(CommonEntities.Aggregation aggregation) {
      switch (aggregation) {
        case AVG:
          return Aggregation.AVG;
        case SUM:
          return Aggregation.SUM;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown aggregation type" + aggregation);
      }
    }
  }
}
