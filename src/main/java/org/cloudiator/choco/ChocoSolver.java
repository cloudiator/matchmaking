package org.cloudiator.choco;

import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage.Literals;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.cloudiator.ocl.DefaultNodeGenerator;
import org.cloudiator.ocl.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.ocl.NodeCandidates;
import org.cloudiator.ocl.OclCsp;
import org.cloudiator.ocl.Solution;
import org.eclipse.emf.ecore.EAttribute;

public class ChocoSolver {

  private static class ChocoSolverInternal {

    private final OclCsp oclCsp;
    private final CloudiatorModel cloudiatorModel;

    private ChocoSolverInternal(OclCsp oclCsp, CloudiatorModel cloudiatorModel) {
      this.oclCsp = oclCsp;
      this.cloudiatorModel = cloudiatorModel;
    }

    public void solve() {

      NodeCandidates nodeCandidates = new DefaultNodeGenerator(new NodeCandidateFactory(),
          cloudiatorModel).getPossibleNodes();
      nodeCandidates.importToModel(cloudiatorModel);

      final ModelGenerationContext modelGenerationContext = new ModelGenerationContext(
          cloudiatorModel,
          new Model(), 5, oclCsp);

      ChocoModelGeneration.visit(modelGenerationContext);

      final Solver solver = modelGenerationContext.getModel().getSolver();
      org.chocosolver.solver.Solution solution = new org.chocosolver.solver.Solution(
          modelGenerationContext.getModel());

      final EAttribute price = (EAttribute) Literals.PRICE.getEStructuralFeature("price");

      IntVar[] priceVariables = modelGenerationContext.getVariableStore().getVariables(price)
          .toArray(new IntVar[modelGenerationContext.nodeSize()]);

      IntVar objectiveFunction = modelGenerationContext.getModel()
          .intVar("objective", 0, IntVar.MAX_INT_BOUND);
      modelGenerationContext.getModel().sum(priceVariables, "<=", objectiveFunction).post();
      modelGenerationContext.getModel().setObjective(Model.MINIMIZE, objectiveFunction);

      solver.limitTime(60000);

      solver.setSearch(Search.defaultSearch(modelGenerationContext.getModel()));
      solver.showContradiction();

      while (solver.solve()) {
        solution.record();
      }

      if (solver.isObjectiveOptimal()) {
        System.out.println("Solved optimally.");
      }

      final Solution ret = ChocoSolutionToSolution.create(modelGenerationContext).apply(solution);
      ret.setTime(solver.getTimeCount());

      System.out.println(ret);

    }

  }


  public void solve(OclCsp oclCsp, CloudiatorModel cloudiatorModel) {
    new ChocoSolverInternal(oclCsp, cloudiatorModel).solve();
  }

}
