package org.cloudiator.matchmaking.choco;

import static de.uniulm.omi.cloudiator.util.StreamUtil.getOnly;

import cloudiator.CloudiatorPackage.Literals;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.chocosolver.solver.variables.IntVar;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.ocl.NodeCandidates;
import org.eclipse.emf.ecore.EClass;

public class ChocoSolutionToSolution implements
    Function<org.chocosolver.solver.Solution, Solution> {

  private final NodeCandidates nodeCandidates;
  private final ModelGenerationContext modelGenerationContext;

  private ChocoSolutionToSolution(NodeCandidates nodeCandidates,
      ModelGenerationContext modelGenerationContext) {
    this.nodeCandidates = nodeCandidates;

    this.modelGenerationContext = modelGenerationContext;
  }

  public static ChocoSolutionToSolution create(NodeCandidates nodeCandidates,
      ModelGenerationContext modelGenerationContext) {
    return new ChocoSolutionToSolution(nodeCandidates, modelGenerationContext);
  }

  @Override
  public Solution apply(org.chocosolver.solver.Solution solution) {

    List<NodeCandidate> nodeCandidateList = new LinkedList<>();

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {

      String nodeId = getId(Literals.NODE, solution, node);
      final NodeCandidate solutionCandidate = nodeCandidates.stream()
          .filter(n -> n.id().equals(nodeId)).collect(getOnly()).orElseThrow(
              () -> new IllegalStateException("Could not find node candidate with id " + nodeId)
          );

      nodeCandidateList.add(solutionCandidate);
    }

    return Solution.of(nodeCandidateList);
  }

  private String getId(EClass eClass, org.chocosolver.solver.Solution solution, int node) {
    return modelGenerationContext.mapBack(
        solution.getIntVal(
            (IntVar) modelGenerationContext.getVariableStore().getIdVariables(node).get(
                eClass)), eClass.getEIDAttribute(), String.class);
  }

}
