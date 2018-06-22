package org.cloudiator.matchmaking.ocl;

import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;
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

  @Inject
  public MatchmakingRequestListener(MessageInterface messageInterface, MetaSolver metaSolver) {
    this.messageInterface = messageInterface;
    this.metaSolver = metaSolver;
  }


  @Override
  public void run() {
    Subscription subscribe = messageInterface
        .subscribe(MatchmakingRequest.class, MatchmakingRequest.parser(),
            (id, matchmakingRequest) -> {

              String userId = matchmakingRequest.getUserId();

              try {

                OclCsp oclCsp = OclCsp
                    .ofRequirements(
                        matchmakingRequest.getRequirements().getRequirementsList().stream()
                            .map(REQUIREMENT_CONVERTER).collect(Collectors.toList()));

                LOGGER.info(
                    String.format("%s has generated the constraint problem %s", this, oclCsp));

                Solution solution = metaSolver.solve(oclCsp, userId);

                if (solution.noSolution()) {
                  messageInterface.reply(MatchmakingResponse.class, id,
                      Error.newBuilder().setCode(400)
                          .setMessage(
                              String
                                  .format("Could not find a solution for the problem %s.", oclCsp))
                          .build());
                  return;
                }

                Builder matchmakingResponseBuilder = MatchmakingResponse.newBuilder();

                solution.getList().forEach(
                    nodeCandidate -> matchmakingResponseBuilder
                        .addNodes(VirtualMachineRequest.newBuilder()
                            .setHardware(nodeCandidate.getHardware().getId())
                            .setImage(nodeCandidate.getImage().getId())
                            .setLocation(nodeCandidate.getLocation().getId()).build()));

                messageInterface.reply(id, matchmakingResponseBuilder.build());

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
}
