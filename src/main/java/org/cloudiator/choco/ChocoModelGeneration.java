package org.cloudiator.choco;

import java.util.ArrayList;
import java.util.List;
import org.cloudiator.choco.ClassAttributeHandler.ClassAttributeContextVisitor;
import org.cloudiator.choco.ClassStructureHandler.ClassStructureHandlerVisitor;
import org.cloudiator.choco.ConstraintHandler.ConstraintHandlerModelGenerationVisitor;
import org.cloudiator.choco.RelationshipHandler.RelationModelVisitor;

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
