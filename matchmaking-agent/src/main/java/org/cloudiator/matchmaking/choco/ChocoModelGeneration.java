package org.cloudiator.matchmaking.choco;

import java.util.ArrayList;
import java.util.List;
import org.cloudiator.matchmaking.choco.ClassAttributeHandler.ClassAttributeContextVisitor;
import org.cloudiator.matchmaking.choco.ClassStructureHandler.ClassStructureHandlerVisitor;
import org.cloudiator.matchmaking.choco.ConstraintHandler.ConstraintHandlerModelGenerationVisitor;
import org.cloudiator.matchmaking.choco.RelationshipHandler.RelationModelVisitor;

public class ChocoModelGeneration {

  private static List<ModelGenerationContextVisitor> modelGenerationContextVisitors = new ArrayList<>();

  static {
    modelGenerationContextVisitors.add(new ClassAttributeContextVisitor());
    modelGenerationContextVisitors.add(new ClassStructureHandlerVisitor());
    modelGenerationContextVisitors.add(new RelationModelVisitor());
    modelGenerationContextVisitors.add(new ConstraintHandlerModelGenerationVisitor());
  }

  public static void visit(ModelGenerationContext modelGenerationContext) {
    modelGenerationContextVisitors.forEach(
        modelGenerationContextVisitor -> modelGenerationContextVisitor
            .visit(modelGenerationContext));
  }


}
