package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkState;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import java.util.ArrayList;
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
import javax.inject.Named;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.domain.Solver;
import org.cloudiator.messages.NodeEntities;
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
  private final int solvingTime;

  @Inject
  public MetaSolver(
      ModelGenerator modelGenerator, Set<Solver> solvers, @Named("solvingTime") int solvingTime) {
    this.modelGenerator = modelGenerator;
    this.solvers = solvers;
    executorService = MoreExecutors
        .listeningDecorator(Executors.newFixedThreadPool(solvers.size()));
    this.solvingTime = solvingTime;
    MoreExecutors.addDelayedShutdownHook(executorService, 1, TimeUnit.MINUTES);
  }

  private Optional<Solution> generateExistingSolution(List<NodeEntities.Node> existingNodes,
      NodeCandidates nodeCandidates) throws ModelGenerationException {

    if (existingNodes.isEmpty()) {
      return Optional.empty();
    }

    List<NodeCandidate> candidates = new ArrayList<>(existingNodes.size());
    for (NodeEntities.Node existingNode : existingNodes) {
      if (Strings.isNullOrEmpty(existingNode.getNodeCandidate())) {
        throw new ModelGenerationException(
            String.format("NodeCandidate for node %s is unknown.", existingNode));
      }
      final Optional<NodeCandidate> existingNodeCandidate = nodeCandidates.stream()
          .filter(nodeCandidate -> nodeCandidate.id().equals(existingNode.getNodeCandidate()))
          .collect(StreamUtil.getOnly());

      candidates.add(existingNodeCandidate.orElseThrow(() -> new ModelGenerationException(String
          .format("NodeCandidate with id %s is no longer valid.",
              existingNode.getNodeCandidate()))));

    }

    return Optional.of(Solution.of(candidates));
  }

  private int deriveNodeSize(List<NodeEntities.Node> existingNodes,
      @Nullable Integer minimumNodeSize) {
    if (minimumNodeSize == null) {
      return existingNodes.size() + 1;
    }
    return minimumNodeSize;
  }

  @Nullable
  public Solution solve(OclCsp csp, List<NodeEntities.Node> existingNodes, String userId)
      throws ModelGenerationException {

    final int nodeSize = deriveNodeSize(existingNodes, csp.getMinimumNodeSize());

    ConstraintChecker cc = ConstraintChecker.create(csp);

    LOGGER.debug(String
        .format("%s is solving CSP %s for user %s with existing nodes %s for target node size %s.",
            this, csp, userId,
            existingNodes, nodeSize));

    NodeGenerator nodeGenerator =
        new ConsistentNodeGenerator(
            NodeCandidateCache.cache(userId, new DefaultNodeGenerator(nodeCandidateFactory,
                modelGenerator.generateModel(userId), new ByonUpdater())),
            cc);

    long startGeneration = System.currentTimeMillis();

    //generate node candidates
    final NodeCandidates possibleNodes = nodeGenerator.get();

    LOGGER.debug(String.format("CSP %s has %s possible candidates.", csp,
        possibleNodes.size()));

    if (possibleNodes.isEmpty()) {
      LOGGER.info(String.format(
          "CSP %s can not have a solution as possible candidates is empty. Returning without solvers.",
          csp));
      return Solution.EMPTY_SOLUTION;
    }

    Optional<Solution> existingSolution = generateExistingSolution(existingNodes, possibleNodes);

    long generationTime = System.currentTimeMillis() - startGeneration;
    LOGGER.info(
        String.format("Possible candidate generation for CSP %s took %s", csp, generationTime));

    LOGGER.info(
        String.format("Start solving of csp: %s using the following solvers: %s", csp,
            Joiner.on(",").join(solvers)));
    long startSolving = System.currentTimeMillis();

    List<Callable<Solution>> solverCallables = new LinkedList<>();
    for (Solver solver : solvers) {
      solverCallables.add(() -> {
        try {
          return solver.solve(csp, possibleNodes, existingSolution.orElse(null), nodeSize);
        } catch (Exception e) {
          LOGGER.error(String.format("Error while executing solver %s on CSP %s", solver, csp), e);
          return null;
        }
      });
    }

    try {

      final List<Future<Solution>> futures = executorService
          .invokeAll(solverCallables, solvingTime, TimeUnit.MINUTES);

      //get all solutions and filter failed ones
      final List<Solution> initialSolutions = futures.stream().map(future -> {
        try {
          checkState(future.isDone(), "Expected future to be done, by contract of invokeAll");
          return future.get();
        } catch (InterruptedException | ExecutionException | CancellationException e) {
          return null;
        }
      }).collect(Collectors.toList());

      long solvingTime = System.currentTimeMillis() - startSolving;

      LOGGER.info(
          String.format("Finished solving of csp: %s.", csp));

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

        LOGGER.info(
            String.format("Found optimal solution %s for csp: %s.", optimalSolution, csp));

        return optimalSolution;
      }

      final Optional<Solution> minOptional = solutions.stream().min(Comparator.naturalOrder());
      if (minOptional.isPresent()) {
        final Solution solution = minOptional.get();
        solution.setTime(solvingTime);

        LOGGER.info(
            String.format("Not found an optimal solution. Using best solution %s for csp: %s.",
                solution, csp));

        return solution;
      }

      LOGGER.info(
          String.format("No solution found for csp: %s.", csp));

      return Solution.EMPTY_SOLUTION;


    } catch (InterruptedException e) {
      LOGGER.warn("Solver got interrupted while solving CSP " + csp, e);
      Thread.currentThread().interrupt();
      return Solution.EMPTY_SOLUTION;
    }
  }

}
