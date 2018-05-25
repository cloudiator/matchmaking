package org.cloudiator.matchmaking.choco;

import static de.uniulm.omi.cloudiator.util.StreamUtil.getOnly;

import cloudiator.CloudiatorPackage.Literals;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.chocosolver.solver.variables.IntVar;
import org.cloudiator.matchmaking.ocl.NodeCandidate;
import org.cloudiator.matchmaking.ocl.NodeCandidates;
import org.cloudiator.matchmaking.ocl.Solution;
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
      String cloudId = getId(Literals.CLOUD, solution, node);
      String imageId = getId(Literals.IMAGE, solution, node);
      String locationId = getId(Literals.LOCATION, solution, node);
      String hardwareId = getId(Literals.HARDWARE, solution, node);

      NodeCandidate nodeCandidate = nodeCandidates.stream().filter(
          n -> n.getCloud().getId().equals(cloudId) && n.getHardware().getId()
              .equals(hardwareId) && n.getLocation().getId().equals(locationId) && n
              .getImage().getId().equals(imageId)).collect(getOnly())
          .orElseThrow(() -> new IllegalStateException(String.format(
              "Could not find node using cloudId %s, imageId %s, hardwareId %s and locationId %s",
              cloudId, imageId, hardwareId, locationId)));
      nodeCandidateList.add(nodeCandidate);
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
