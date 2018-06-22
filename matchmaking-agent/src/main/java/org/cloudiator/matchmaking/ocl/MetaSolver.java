package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkState;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.domain.Solver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MetaSolver {

  private static final CloudiatorFactory cloudiatorFactory = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private static final NodeCandidateFactory nodeCandidateFactory = NodeCandidateFactory.create();
  private static final Logger LOGGER = LoggerFactory.getLogger(MetaSolver.class);
  private final Set<Solver> solvers;
  private final ListeningExecutorService executorService;
  private final ModelGenerator modelGenerator;

  @Inject
  public MetaSolver(
      ModelGenerator modelGenerator, Set<Solver> solvers) {
    this.modelGenerator = modelGenerator;
    this.solvers = solvers;
    executorService = MoreExecutors
        .listeningDecorator(Executors.newFixedThreadPool(solvers.size()));
    MoreExecutors.addDelayedShutdownHook(executorService, 1, TimeUnit.MINUTES);
  }

  @Nullable
  public Solution solve(OclCsp csp, String userId) throws ModelGenerationException {

    ConstraintChecker cc = ConstraintChecker.create(csp);

    LOGGER.debug(String.format("%s is solving CSP %s for user %s", this, csp, userId));

    NodeGenerator nodeGenerator =
        new ConsistentNodeGenerator(
            new DefaultNodeGenerator(nodeCandidateFactory, modelGenerator.generateModel(userId)),
            cc);

    long startGeneration = System.currentTimeMillis();

    //generate node candidates
    final NodeCandidates possibleNodes = nodeGenerator.getPossibleNodes();

    LOGGER.debug(String.format("CSP %s has %s possible candidates.", csp,
        possibleNodes.size()));

    if (possibleNodes.isEmpty()) {
      LOGGER.info(String.format(
          "CSP %s can not have a solution as possible candidates is empty. Returning without solvers.",
          csp));
      return Solution.EMPTY_SOLUTION;
    }

    long generationTime = System.currentTimeMillis() - startGeneration;
    LOGGER.info(
        String.format("Possible candidate generation for CSP %s took %s", csp, generationTime));

    long startSolving = System.currentTimeMillis();

    List<Callable<Solution>> solverCallables = new LinkedList<>();
    for (Solver solver : solvers) {
      solverCallables.add(() -> {
        try {
          return solver.solve(csp, possibleNodes);
        } catch (Exception e) {
          LOGGER.error(String.format("Error while executing solver %s on CSP %s", solver, csp), e);
          return null;
        }
      });
    }

    try {

      long solvingTime = System.currentTimeMillis() - startSolving;

      final List<Future<Solution>> futures = executorService
          .invokeAll(solverCallables, 5, TimeUnit.MINUTES);

      //get all solutions and filter failed ones
      final List<Solution> initialSolutions = futures.stream().map(future -> {
        try {
          checkState(future.isDone(), "Expected future to be done, by contract of invokeAll");
          return future.get();
        } catch (InterruptedException | ExecutionException | CancellationException e) {
          return null;
        }
      }).collect(Collectors.toList());
      final List<Solution> solutions = initialSolutions.stream().filter(
          Objects::nonNull).filter(solution -> !solution.noSolution()).collect(Collectors.toList());

      final Optional<Solution> anyOptimalSolution = solutions.stream()
          .filter(new Predicate<Solution>() {
            @Override
            public boolean test(Solution solution) {
              return solution.isOptimal();
            }
          }).findAny();
      if (anyOptimalSolution.isPresent()) {
        final Solution optimalSolution = anyOptimalSolution.get();
        optimalSolution.setTime(solvingTime);
        return optimalSolution;
      }

      final Optional<Solution> minOptional = solutions.stream().min(Comparator.naturalOrder());
      if (minOptional.isPresent()) {
        final Solution solution = minOptional.get();
        solution.setTime(solvingTime);
        return solution;
      }

      return Solution.EMPTY_SOLUTION;


    } catch (InterruptedException e) {
      LOGGER.warn("Solver got interrupted while solving CSP " + csp, e);
      Thread.currentThread().interrupt();
      return Solution.EMPTY_SOLUTION;
    }
  }

}
