package org.cloudiator.matchmaking.ocl;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import javax.inject.Inject;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.matchmaking.converters.SolutionConverter;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse.Builder;
import org.cloudiator.messages.entities.Matchmaking.SolutionRequest;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SolutionRequestListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SolutionRequestListener.class);
  private final MessageInterface messageInterface;
  private static final SolutionConverter SOLUTION_CONVERTER = SolutionConverter.INSTANCE;
  private final SolutionCache solutionCache;

  @Inject
  public SolutionRequestListener(MessageInterface messageInterface,
      SolutionCache solutionCache) {
    this.messageInterface = messageInterface;
    this.solutionCache = solutionCache;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).toString();
  }

  @Override
  public void run() {
    Subscription subscribe = messageInterface
        .subscribe(SolutionRequest.class, SolutionRequest.parser(),
            (id, solutionRequest) -> {

              LOGGER.info(String
                  .format("%s received new solution request %s.", this, solutionRequest));

              String userId = solutionRequest.getUserId();

              try {

                final Optional<Solution> retrieve = solutionCache
                    .retrieve(userId, solutionRequest.getSolution());

                if (!retrieve.isPresent()) {

                  messageInterface.reply(MatchmakingResponse.class, id,
                      Error.newBuilder().setCode(404).setMessage(String
                          .format("Solution with id %s could not be found.",
                              solutionRequest.getSolution())).build());

                  return;
                }

                replyWithSolution(id, retrieve.get());


              } catch (Exception e) {
                LOGGER.error(String.format("Error while receiving solution: %s.", e.getMessage()),
                    e);
                messageInterface.reply(MatchmakingResponse.class, id,
                    Error.newBuilder().setCode(500)
                        .setMessage(
                            String
                                .format("An error occurred while retrieving solution: %s",
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
