package org.cloudiator.matchmaking.ocl;

import com.google.common.base.MoreObjects;
import de.uniulm.omi.cloudiator.sword.domain.QuotaSet;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.messaging.QuotaConverter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Named;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.matchmaking.converters.SolutionConverter;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.messages.Cloud.QuotaQueryRequest;
import org.cloudiator.messages.Cloud.QuotaQueryResponse;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingRequest;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse.Builder;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.messaging.services.CloudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchmakingRequestListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(MatchmakingRequestListener.class);
  private final MessageInterface messageInterface;
  private final MetaSolver metaSolver;
  private static final RequirementConverter REQUIREMENT_CONVERTER = RequirementConverter.INSTANCE;
  private static final SolutionConverter SOLUTION_CONVERTER = SolutionConverter.INSTANCE;
  private static final NodeToNodeMessageConverter NODE_CONVERTER = NodeToNodeMessageConverter.INSTANCE;
  private static final QuotaConverter QUOTA_CONVERTER = QuotaConverter.INSTANCE;
  private final SolutionCache solutionCache;
  private final CloudService cloudService;
  private final boolean considerQuota;

  @Inject
  public MatchmakingRequestListener(MessageInterface messageInterface, MetaSolver metaSolver,
      SolutionCache solutionCache, CloudService cloudService,
      @Named("considerQuota") boolean considerQuota) {
    this.messageInterface = messageInterface;
    this.metaSolver = metaSolver;
    this.solutionCache = solutionCache;
    this.cloudService = cloudService;
    this.considerQuota = considerQuota;
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
              Integer targetNodeSize;
              if (matchmakingRequest.getMinimumNodeSize() == 0) {
                targetNodeSize = null;
              } else {
                targetNodeSize = matchmakingRequest.getMinimumNodeSize();
              }

              List<Node> existingNodes = matchmakingRequest.getExistingNodesList().stream()
                  .map(NODE_CONVERTER::applyBack).collect(Collectors.toList());

              try {

                QuotaSet quotaSet;

                if (considerQuota) {
                  final QuotaQueryResponse quotaQueryResponse = cloudService.queryQuota(
                      QuotaQueryRequest.newBuilder().setUserId(matchmakingRequest.getUserId())
                          .build());

                  quotaSet = new QuotaSet(
                      quotaQueryResponse.getQuotasList().stream().map(QUOTA_CONVERTER)
                          .collect(Collectors.toSet()));
                } else {
                  quotaSet = QuotaSet.EMPTY;
                }

                OclCsp oclCsp = OclCsp
                    .ofRequirements(
                        matchmakingRequest.getNodeRequirements().getRequirementsList().stream()
                            .map(REQUIREMENT_CONVERTER).collect(Collectors.toList()), existingNodes,
                        quotaSet,
                        targetNodeSize);

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
                    .solve(oclCsp, userId);

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
