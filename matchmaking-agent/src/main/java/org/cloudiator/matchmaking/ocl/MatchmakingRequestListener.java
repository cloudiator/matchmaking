package org.cloudiator.matchmaking.ocl;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.matchmaking.converters.SolutionConverter;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingRequest;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse.Builder;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchmakingRequestListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(MatchmakingRequestListener.class);
  private final MessageInterface messageInterface;
  private final MetaSolver metaSolver;
  private static final RequirementConverter REQUIREMENT_CONVERTER = RequirementConverter.INSTANCE;
  private static final SolutionConverter SOLUTION_CONVERTER = SolutionConverter.INSTANCE;
  private final SolutionCache solutionCache;

  @Inject
  public MatchmakingRequestListener(MessageInterface messageInterface, MetaSolver metaSolver,
      SolutionCache solutionCache) {
    this.messageInterface = messageInterface;
    this.metaSolver = metaSolver;
    this.solutionCache = solutionCache;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }

  @Override
  public void run() {
    Subscription subscribe = messageInterface
        .subscribe(MatchmakingRequest.class, MatchmakingRequest.parser(),
            (id, matchmakingRequest) -> {

              LOGGER.info(String
                  .format("%s received new matchmaking request %s.", this, matchmakingRequest));

              String userId = matchmakingRequest.getUserId();

              try {

                OclCsp oclCsp = OclCsp
                    .ofRequirements(
                        matchmakingRequest.getNodeRequirements().getRequirementsList().stream()
                            .map(REQUIREMENT_CONVERTER).collect(Collectors.toList()));

                LOGGER.info(
                    String
                        .format("%s has generated the constraint problem %s.", this,
                            oclCsp));

                final Optional<Solution> cachedSolution = solutionCache.retrieve(userId, oclCsp);

                if (cachedSolution.isPresent() && cachedSolution.get().isValid()) {

                  LOGGER.info(
                      String
                          .format("%s found existing solution %s for the constraint problem %s.",
                              this, cachedSolution.get(),
                              oclCsp));

                  replyWithSolution(id, cachedSolution.get());

                  return;
                }

                Solution solution = metaSolver
                    .solve(oclCsp, matchmakingRequest.getExistingNodesList(), userId);

                if (solution == null || solution.noSolution()) {
                  LOGGER
                      .warn(
                          String.format("%s could not find a solution for csp %s.", this, oclCsp));
                  messageInterface.reply(MatchmakingResponse.class, id,
                      Error.newBuilder().setCode(400)
                          .setMessage(
                              String
                                  .format("Could not find a solution for the problem %s.", oclCsp))
                          .build());
                  return;
                }

                solutionCache.storeSolution(userId, oclCsp, solution);

                LOGGER.info(String
                    .format("%s found a solution %s for the csp %s.", this, solution,
                        oclCsp));

                replyWithSolution(id, solution);


              } catch (Exception e) {
                LOGGER.error(String.format("Error while solving the problem: %s.", e.getMessage()),
                    e);
                messageInterface.reply(MatchmakingResponse.class, id,
                    Error.newBuilder().setCode(500)
                        .setMessage(
                            String
                                .format("An error occurred while solving the problem: %s",
                                    e.getMessage()))
                        .build());
              }

            });
  }

  private void replyWithSolution(String requestId, Solution solution) {
    Builder matchmakingResponseBuilder = MatchmakingResponse.newBuilder();
    matchmakingResponseBuilder.setSolution(SOLUTION_CONVERTER.apply(solution));
    messageInterface.reply(requestId, matchmakingResponseBuilder.build());
  }

}
