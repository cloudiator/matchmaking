package org.cloudiator.choco;

import static de.uniulm.omi.cloudiator.util.StreamUtil.getOnly;

import cloudiator.CloudiatorPackage.Literals;
import cloudiator.Node;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import org.chocosolver.solver.variables.IntVar;
import org.cloudiator.ocl.NodeCandidate;
import org.cloudiator.ocl.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.ocl.Solution;
import org.eclipse.emf.ecore.EClass;

public class ChocoSolutionToSolution implements
    Function<org.chocosolver.solver.Solution, Solution> {

  private final ModelGenerationContext modelGenerationContext;

  private ChocoSolutionToSolution(ModelGenerationContext modelGenerationContext) {
    this.modelGenerationContext = modelGenerationContext;
  }

  public static ChocoSolutionToSolution create(ModelGenerationContext modelGenerationContext) {
    return new ChocoSolutionToSolution(modelGenerationContext);
  }

  @Override
  public Solution apply(org.chocosolver.solver.Solution solution) {

    NodeCandidateFactory nodeCandidateFactory = new NodeCandidateFactory();
    List<NodeCandidate> nodeCandidateList = new LinkedList<>();

    for (int node = 1; node <= modelGenerationContext.nodeSize(); node++) {
      String cloudId = getId(Literals.CLOUD, solution, node);
      String imageId = getId(Literals.IMAGE, solution, node);
      String locationId = getId(Literals.LOCATION, solution, node);
      String hardwareId = getId(Literals.HARDWARE, solution, node);

      Node modelNode = modelGenerationContext.getCloudiatorModel().getNodes().stream().filter(
          n -> n.getCloud().getId().equals(cloudId) && n.getHardware().getId()
              .equals(hardwareId) && n.getLocation().getId().equals(locationId) && n
              .getImage().getId().equals(imageId)).collect(getOnly())
          .orElseThrow(() -> new IllegalStateException(String.format(
              "Could not find node using cloudId %s, imageId %s, hardwareId %s and locationId %s",
              cloudId, imageId, hardwareId, locationId)));

      final NodeCandidate nodeCandidate = nodeCandidateFactory
          .of(modelNode.getCloud(), modelNode.getHardware(), modelNode.getImage(),
              modelNode.getLocation(), modelNode.getPrice());
      nodeCandidateList.add(nodeCandidate);
    }

    final Solution ret = Solution.of(nodeCandidateList);
    return ret;
  }

  private String getId(EClass eClass, org.chocosolver.solver.Solution solution, int node) {
    return modelGenerationContext.mapBack(
        solution.getIntVal(
            (IntVar) modelGenerationContext.getVariableStore().getIdVariables(node).get(
                eClass)), eClass.getEIDAttribute(), String.class);
  }

}
