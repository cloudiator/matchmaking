package org.cloudiator.matchmaking.ocl;

import cloudiator.CloudiatorModel;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import io.github.cloudiator.domain.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Named;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.domain.Solver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SolverHandler {

  private static final NodeCandidateFactory nodeCandidateFactory = NodeCandidateFactory.create();
  private static final Logger LOGGER = LoggerFactory.getLogger(SolverHandler.class);
  private final Set<Solver> solvers;

  private final ModelGenerator modelGenerator;
  private final int solvingTime;
  private final ByonCache byonCache;

  @Inject
  public SolverHandler(
      ModelGenerator modelGenerator, Set<Solver> solvers, @Named("solvingTime") int solvingTime,
      ByonCache byonCache) {
    this.modelGenerator = modelGenerator;
    this.solvers = solvers;
    this.byonCache = byonCache;
    this.solvingTime = solvingTime;

  }

  private Optional<Solution> generateExistingSolution(List<Node> existingNodes,
      NodeCandidates nodeCandidates) throws ModelGenerationException {

    if (existingNodes.isEmpty()) {
      return Optional.empty();
    }

    List<NodeCandidate> candidates = new ArrayList<>(existingNodes.size());
    for (Node existingNode : existingNodes) {
      if (!existingNode.nodeCandidate().isPresent()) {
        throw new ModelGenerationException(
            String.format("NodeCandidate for node %s is unknown.", existingNode));
      }
      final Optional<NodeCandidate> existingNodeCandidate = nodeCandidates.stream()
          .filter(nodeCandidate -> nodeCandidate.id().equals(existingNode.nodeCandidate().get()))
          .collect(StreamUtil.getOnly());

      candidates.add(existingNodeCandidate.orElseThrow(() -> new ModelGenerationException(String
          .format("NodeCandidate with id %s is no longer valid.",
              existingNode.nodeCandidate().get()))));

    }

    return Optional.of(Solution.of(candidates));
  }

  private int deriveNodeSize(List<Node> existingNodes,
      @Nullable Integer minimumNodeSize) {
    if (minimumNodeSize == null) {
      return existingNodes.size() + 1;
    }
    return minimumNodeSize;
  }

  @Nullable
  public synchronized Solution solve(OclCsp csp, String userId)
      throws ModelGenerationException {

    final int nodeSize = deriveNodeSize(csp.getExistingNodes(), csp.getMinimumNodeSize());

    ConstraintChecker cc = ConstraintChecker.create(csp);

    LOGGER.debug(String
        .format("%s is solving CSP %s for user %s for target node size %s.",
            this, csp, userId,
            nodeSize));

    final CloudiatorModel cloudiatorModel = modelGenerator.generateModel(userId);
    NodeGenerator nodeGenerator =
        new QuotaFilter(
            cloudiatorModel, new ConsistentNodeGenerator(
            NodeCandidateCache
                .cache(userId,
                    new DefaultNodeGenerator(nodeCandidateFactory, cloudiatorModel, byonCache
                    )),
            cc), csp.getQuotaSet());

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

    Optional<Solution> existingSolution = generateExistingSolution(csp.getExistingNodes(),
        possibleNodes);

    long generationTime = System.currentTimeMillis() - startGeneration;
    LOGGER.info(
        String.format("Possible candidate generation for CSP %s took %s", csp, generationTime));

    LOGGER.info(
        String.format("Start solving of csp: %s using the following solvers: %s", csp,
            Joiner.on(",").join(solvers)));

    MetaSolver metaSolver = new MetaSolver(solvers, solvingTime);

    try {
      final Solution solve = metaSolver
          .solve(csp, possibleNodes, existingSolution.orElse(null), nodeSize);

      if (solve.isEmpty()) {
        LOGGER.info(
            String.format("No solution found for csp: %s.", csp));
      } else if (solve.isOptimal()) {
        LOGGER.info(
            String.format("Found optimal solution %s for csp: %s.", solve, csp));
      } else {
        LOGGER.info(
            String.format("Not found an optimal solution. Using best solution %s for csp: %s.",
                solve, csp));
      }

      //evict the cache
      byonCache.evictBySolution(solve, userId);

      return solve;


    } catch (InterruptedException e) {
      LOGGER.warn("Interrupted while solving, returning emtpy solution");
      return Solution.EMPTY_SOLUTION;
    }
  }

}
