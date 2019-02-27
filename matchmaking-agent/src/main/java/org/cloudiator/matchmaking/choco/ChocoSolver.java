package org.cloudiator.matchmaking.choco;

import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage.Literals;
import com.google.common.base.MoreObjects;
import java.util.LinkedList;
import javax.annotation.Nullable;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.ocl.NodeCandidates;
import org.cloudiator.matchmaking.ocl.OclCsp;
import org.eclipse.emf.ecore.EAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChocoSolver implements org.cloudiator.matchmaking.domain.Solver {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChocoSolver.class);

  private static class ChocoSolverInternal {

    private final OclCsp oclCsp;
    private final CloudiatorModel cloudiatorModel;
    private final NodeCandidates nodeCandidates;

    private ChocoSolverInternal(OclCsp oclCsp, CloudiatorModel cloudiatorModel,
        NodeCandidates nodeCandidates) {
      this.oclCsp = oclCsp;
      this.cloudiatorModel = cloudiatorModel;
      this.nodeCandidates = nodeCandidates;
    }

    Solution solve(int numberOfNodes, @Nullable Solution existingSolution) {

      final ModelGenerationContext modelGenerationContext = new ModelGenerationContext(
          cloudiatorModel,
          new Model(), numberOfNodes, oclCsp, existingSolution);

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

      //solver.setSearch(Search.defaultSearch(modelGenerationContext.getModel()));
      solver.setSearch(
          Search.activityBasedSearch(modelGenerationContext.getModel().retrieveIntVars(true)));

      final LinkedList<ContradictionException> contradictions = new LinkedList<>();

      solver.plugMonitor((IMonitorContradiction) contradictions::add);
      solver.limitSearch(() -> Thread.currentThread().isInterrupted());

      while (solver.solve()) {
        solution.record();
      }

      if (solver.getSolutionCount() == 0) {
        LOGGER.trace(String.format("%s could not find a solution.", contradictions.getLast()));
        return Solution.EMPTY_SOLUTION;
      }

      final Solution ret = ChocoSolutionToSolution.create(nodeCandidates, modelGenerationContext)
          .apply(solution);

      if (solver.isObjectiveOptimal()) {
        ret.setIsOptimal(true);
      }

      ret.setTime(solver.getTimeCount());

      return ret;

    }

  }

  private CloudiatorModel generateSolvingModel(NodeCandidates nodeCandidates) {

    SolvingModelGenerator solvingModelGenerator = new SolvingModelGenerator();
    return solvingModelGenerator.apply(nodeCandidates);
  }


  public Solution solve(OclCsp oclCsp, NodeCandidates nodeCandidates,
      @Nullable Solution existingSolution) {

    CloudiatorModel solverModel = generateSolvingModel(nodeCandidates);

    int i = 1;

    while (!Thread.currentThread().isInterrupted()) {
      final ChocoSolverInternal chocoSolverInternal = new ChocoSolverInternal(oclCsp,
          solverModel, nodeCandidates);
      Solution solution = chocoSolverInternal.solve(i, existingSolution);
      if (!solution.noSolution()) {
        return solution;
      }
      i++;
    }
    return Solution.EMPTY_SOLUTION;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
