package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.domain.Solver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetaSolver implements Solver {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetaSolver.class);
  private final Set<Solver> solvers;
  private final ListeningExecutorService executorService;
  private final int solvingTime;

  public MetaSolver(Set<Solver> solvers, int solvingTime) {
    this.solvers = solvers;
    this.solvingTime = solvingTime;
    executorService = MoreExecutors
        .listeningDecorator(Executors.newCachedThreadPool());
    MoreExecutors.addDelayedShutdownHook(executorService, 1, TimeUnit.MINUTES);
  }

  private class SolutionCollector implements Consumer<Solution> {

    private final CountDownLatch countDownLatch;
    private List<Solution> collectedSolutions = new LinkedList<>();

    private SolutionCollector(int numberOfSolvers) {
      countDownLatch = new CountDownLatch(numberOfSolvers);
    }

    @Override
    public void accept(Solution solution) {
      LOGGER.debug(
          String.format("MetaSolver received new solution by solver %s", solution.getSolver()));
      LOGGER.debug(String.format("Missing %s solvers", countDownLatch.getCount() - 1));
      if (solution.isOptimal()) {
        LOGGER.debug(String.format("Received solution by solver %s is optimal. Exiting early",
            solution.getSolver()));
        collectedSolutions.add(solution);
        //directly count down
        while (countDownLatch.getCount() > 0) {
          countDownLatch.countDown();
        }
      } else if (solution.isEmpty()) {
        fail();
      } else {
        collectedSolutions.add(solution);
        countDownLatch.countDown();
      }
    }

    public List<Solution> waitFor(long l, TimeUnit timeUnit) throws InterruptedException {
      countDownLatch.await(l, timeUnit);
      return collectedSolutions;
    }


    public void fail() {
      countDownLatch.countDown();
    }
  }

  private class SolutionCallback implements FutureCallback<Solution> {

    private final Solver solver;
    private final long startTime;
    private final SolutionCollector solutionCollector;

    private SolutionCallback(Solver solver, long startTime,
        SolutionCollector solutionCollector) {
      this.solver = solver;
      this.startTime = startTime;
      this.solutionCollector = solutionCollector;
    }

    @Override
    public void onSuccess(@Nullable Solution solution) {
      checkNotNull(solution, "solution is null");
      long solvingTime = System.currentTimeMillis() - startTime;
      solution.setTime(solvingTime);
      solutionCollector.accept(solution);
    }

    @Override
    public void onFailure(Throwable throwable) {
      LOGGER.warn(String.format("Solver %s failed to find a solution due to an error.", solver),
          throwable);
      solutionCollector.fail();
    }
  }


  @Override
  public Solution solve(OclCsp oclCsp, NodeCandidates nodeCandidates,
      @Nullable Solution existingSolution, @Nullable Integer targetNodeSize)
      throws InterruptedException {

    LOGGER.debug(String.format("MetaSolver is using %s solvers: %s", solvers.size(), solvers));

    long startSolving = System.currentTimeMillis();

    SolutionCollector solutionCollector = new SolutionCollector(solvers.size());

    for (Solver solver : solvers) {
      final ListenableFuture<Solution> solutionFuture = executorService
          .submit(wrapSolverCall(solver, oclCsp, nodeCandidates, existingSolution, targetNodeSize));
      Futures.addCallback(solutionFuture,
          new SolutionCallback(solver, startSolving, solutionCollector));
    }

    try {
      final List<Solution> solutions = solutionCollector.waitFor(solvingTime, TimeUnit.MINUTES);
      //find best solution
      final Optional<Solution> minOptional = solutions.stream().min(Comparator.naturalOrder());

      return minOptional.orElse(Solution.EMPTY_SOLUTION);

    } catch (InterruptedException e) {
      LOGGER.warn("MetaSolver got interrupted while searching for solution");
      throw e;
    } finally {
      executorService.shutdownNow();
    }
  }

  private Callable<Solution> wrapSolverCall(Solver solver, OclCsp oclCsp,
      NodeCandidates nodeCandidates, @Nullable Solution existingSolution,
      @Nullable Integer targetNodeSize) {
    return new Callable<Solution>() {
      @Override
      public Solution call() throws Exception {
        return solver.solve(oclCsp, nodeCandidates, existingSolution, targetNodeSize);
      }
    };
  }
}
