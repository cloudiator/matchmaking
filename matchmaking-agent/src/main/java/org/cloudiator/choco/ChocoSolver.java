package org.cloudiator.choco;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage.Literals;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.cloudiator.ocl.NodeCandidate;
import org.cloudiator.ocl.NodeCandidates;
import org.cloudiator.ocl.OclCsp;
import org.cloudiator.ocl.Solution;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class ChocoSolver implements org.cloudiator.Solver {

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

    public Solution solve(int numberOfNodes, @Nullable Solution existingSolution) {

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

      while (solver.solve()) {
        solution.record();
      }

      if (solver.getSolutionCount() == 0) {
        return Solution.EMPTY_SOLUTION;
      }

      final Solution ret = ChocoSolutionToSolution.create(nodeCandidates, modelGenerationContext)
          .apply(solution);

      if (solver.isObjectiveOptimal()) {
        System.out.println("Solved optimally.");
        ret.setIsOptimal(true);
      }

      ret.setTime(solver.getTimeCount());

      System.out.println(ret);

      return ret;

    }

  }

  private CloudiatorModel generateSolvingModel(NodeCandidates nodeCandidates) {
    final CloudiatorModel ret = CloudiatorFactory.eINSTANCE.createCloudiatorModel();

    Set<Cloud> cloudSet = new HashSet<>();

    Set<String> imageIds = new HashSet<>();
    Set<String> locationIds = new HashSet<>();
    Set<String> hardwareIds = new HashSet<>();

    for (NodeCandidate nodeCandidate : nodeCandidates) {
      cloudSet.add(nodeCandidate.getCloud());
      imageIds.add(nodeCandidate.getImage().getId());
      locationIds.add(nodeCandidate.getLocation().getId());
      hardwareIds.add(nodeCandidate.getHardware().getId());
    }

    cloudSet.forEach(cloud -> ret.getClouds().add(EcoreUtil.copy(cloud)));

    ret.getClouds().stream().flatMap(
        (Function<Cloud, Stream<Image>>) cloud -> cloud.getImages().stream()).map(
        Image::getOperatingSystem).forEach(
        operatingSystem -> ret.getOperatingsystems().add(operatingSystem));

    ret.getClouds().forEach(new Consumer<Cloud>() {
      @Override
      public void accept(Cloud cloud) {

        Set<Image> imagesToBeRemoved = new HashSet<>();
        Set<Location> locationsToBeRemoved = new HashSet<>();
        Set<Hardware> hardwareToBeRemoved = new HashSet<>();

        cloud.getImages().forEach(new Consumer<Image>() {
          @Override
          public void accept(Image image) {
            if (!imageIds.contains(image.getId())) {
              imagesToBeRemoved.add(image);
            }
          }
        });

        cloud.getLocations().forEach(new Consumer<Location>() {
          @Override
          public void accept(Location location) {
            if (!locationIds.contains(location.getId())) {
              locationsToBeRemoved.add(location);
            }
          }
        });

        cloud.getHardwareList().forEach(new Consumer<Hardware>() {
          @Override
          public void accept(Hardware hardware) {
            if (!hardwareIds.contains(hardware.getId())) {
              hardwareToBeRemoved.add(hardware);
            }
          }
        });

        //remove everything
        cloud.getImages().removeAll(imagesToBeRemoved);
        cloud.getHardwareList().removeAll(hardwareToBeRemoved);
        cloud.getLocations().removeAll(locationsToBeRemoved);

      }
    });

    return ret;
  }


  public Solution solve(OclCsp oclCsp, NodeCandidates nodeCandidates) {

    CloudiatorModel solverModel = generateSolvingModel(nodeCandidates);

    int i = 1;

    Solution solution = null;
    while (true) {
      final ChocoSolverInternal chocoSolverInternal = new ChocoSolverInternal(oclCsp,
          solverModel, nodeCandidates);
      solution = chocoSolverInternal.solve(i, solution);
      if (!solution.noSolution()) {
        return solution;
      } else {
        solution = null;
      }
      i++;
    }
  }

}
