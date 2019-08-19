package org.cloudiator.matchmaking.choco;

import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage.Literals;
import com.google.common.base.MoreObjects;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.lns.INeighborFactory;
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

      final long startGeneration = System.currentTimeMillis();

      ChocoModelGeneration.visit(modelGenerationContext);

      final long stopGeneration = System.currentTimeMillis();

      LOGGER.debug(
          "Generation of choco solver model took " + (stopGeneration - startGeneration) + " ms for "
              + numberOfNodes + " nodes.");

      final Solver solver = modelGenerationContext.getModel().getSolver();
      org.chocosolver.solver.Solution solution = new org.chocosolver.solver.Solution(
          modelGenerationContext.getModel());

      final EAttribute price = (EAttribute) Literals.NODE.getEStructuralFeature("price");

      IntVar[] priceVariables = modelGenerationContext.getVariableStore().getVariables(price)
          .toArray(new IntVar[modelGenerationContext.nodeSize()]);

      IntVar objectiveFunction = modelGenerationContext.getModel()
          .intVar("objective", 0, IntVar.MAX_INT_BOUND);
      modelGenerationContext.getModel().sum(priceVariables, "<=", objectiveFunction).post();
      modelGenerationContext.getModel().setObjective(Model.MINIMIZE, objectiveFunction);

      //solver.setSearch(Search.defaultSearch(modelGenerationContext.getModel()));

      solver.setLNS(INeighborFactory.propagationGuided());


      //solver.setSearch(Search.intVarSearch(variables -> {

      //      List<IntVar> uninstantiatedVariables = Arrays.stream(variables)
      //          .filter(v -> !v.isInstantiated())
      //          .collect(Collectors.toList());

      //      if (uninstantiatedVariables.isEmpty()) {
      //        return null;
      //      }

      //      return uninstantiatedVariables.stream().min(
      //          Comparator.comparingInt(IntVar::getValue)).get();
      //    }, IntVar::getLB, priceVariables),
      //    Search.defaultSearch(modelGenerationContext.getModel()));

      //solver.setSearch(
      //    Search.activityBasedSearch(modelGenerationContext.getModel().retrieveIntVars(true)));

      final LinkedList<ContradictionException> contradictions = new LinkedList<>();

      solver.plugMonitor((IMonitorContradiction) contradictions::add);
      solver.limitSearch(() -> Thread.currentThread().isInterrupted());

      while (solver.solve()) {
        solution.record();
      }

      solver.printStatistics();

      if (solver.getSolutionCount() == 0) {
        LOGGER.debug(String.format("%s could not find a solution.", this));
        if (!contradictions.isEmpty()) {
          LOGGER.debug("Last contradiction " + contradictions.getLast());
        }
        return Solution.EMPTY_SOLUTION;
      }

      final Solution ret = ChocoSolutionToSolution.create(nodeCandidates, modelGenerationContext)
          .apply(solution);
      ret.setSolver(ChocoSolver.class);

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

  @Override
  public Solution solve(OclCsp oclCsp, NodeCandidates nodeCandidates,
      @Nullable Solution existingSolution, @Nullable Integer targetNodeSize) {

    final long start = System.currentTimeMillis();

    CloudiatorModel solverModel = generateSolvingModel(nodeCandidates);

    final long stop = System.currentTimeMillis();

    LOGGER.debug("Generation of solving model took " + (stop - start) + " ms");

    if (targetNodeSize == null) {
      targetNodeSize = 1;
    }

    while (!Thread.currentThread().isInterrupted()) {
      final ChocoSolverInternal chocoSolverInternal = new ChocoSolverInternal(oclCsp,
          solverModel, nodeCandidates);
      Solution solution = chocoSolverInternal.solve(targetNodeSize, existingSolution);
      if (!solution.noSolution()) {
        return solution;
      }
      targetNodeSize++;
    }
    return Solution.EMPTY_SOLUTION;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }
}
