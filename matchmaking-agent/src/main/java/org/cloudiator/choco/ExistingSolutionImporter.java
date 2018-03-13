package org.cloudiator.choco;

public class ExistingSolutionImporter {


  private final ModelGenerationContext modelGenerationContext;

  public ExistingSolutionImporter(
      ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
  }

  public void handle() {
    if (!modelGenerationContext.getExistingSolution().isPresent()) {
      return;
    }


  }

  private static class ExistingSolutionModelGenerationContextVisitor implements
      ModelGenerationContextVisitor {

    @Override
    public void visit(ModelGenerationContext modelGenerationContext) {
      new ExistingSolutionImporter(modelGenerationContext).handle();
    }
  }

}
