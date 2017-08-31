package org.cloudiator.ocl;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import com.google.inject.Inject;
import org.cloudiator.ocl.NodeCandidate.NodeCandidateFactory;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solver {

  private static final CloudiatorFactory cloudiatorFactory = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private static final NodeCandidateFactory nodeCandidateFactory = new NodeCandidateFactory(
      cloudiatorFactory);
  private static final Logger LOGGER = LoggerFactory.getLogger(Solver.class);

  private final ModelGenerator modelGenerator;

  @Inject
  public Solver(ModelGenerator modelGenerator) {
    this.modelGenerator = modelGenerator;
  }

  Solution solve(ConstraintSatisfactionProblem csp, String userId) throws ParserException {
    ConstraintChecker cc = new ConstraintChecker(csp);

    LOGGER.debug(String.format("%s is solving CSP %s for user %s", this, csp, userId));

    NodeGenerator nodeGenerator = new DifferentNodeGenerator(
        new ConsistentNodeGenerator(
            new DefaultNodeGenerator(nodeCandidateFactory, modelGenerator.generateModel(userId)),
            cc), csp);
    SolutionGenerator solutionGenerator = new SolutionGenerator(nodeGenerator);
    long startGeneration = System.currentTimeMillis();

    LOGGER.debug(String.format("CSP %s has %s initial solutions.", csp,
        solutionGenerator.generateInitialSolutions().size()));

    long generationTime = System.currentTimeMillis() - startGeneration;
    System.out.println(
        String.format("Possible Solution Generation for CSP %s took %s", csp, generationTime));

    BestFit bestFit = new BestFit(solutionGenerator, cc, 10000,
        1);

    long startSolving = System.currentTimeMillis();

    Solution solution = bestFit.solve();

    long solvingTime = System.currentTimeMillis() - startSolving;

    LOGGER.debug(String
        .format("%s found a solution %s for CSP %s in %s milliseconds", this, solution, csp,
            solvingTime));

    return solution;
  }

}
