package org.cloudiator.matchmaking.choco;

public class RelevantClassHandler implements ModelGenerationContextVisitor {

  //todo implement, should find relevant classes to the problem.

  private static class RelevantClassHandlerInternal {

    private final ModelGenerationContext modelGenerationContext;

    private RelevantClassHandlerInternal(ModelGenerationContext modelGenerationContext) {
      this.modelGenerationContext = modelGenerationContext;
    }

    public void handle() {

    }


  }


  @Override
  public void visit(ModelGenerationContext modelGenerationContext) {

  }
}
