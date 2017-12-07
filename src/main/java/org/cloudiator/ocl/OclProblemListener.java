package org.cloudiator.ocl;

import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.entities.CommonEntities.OclRequirement;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse;
import org.cloudiator.messages.entities.Matchmaking.MatchmakingResponse.Builder;
import org.cloudiator.messages.entities.Matchmaking.OclSolutionRequest;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OclProblemListener implements Runnable {

  private final MessageInterface messageInterface;
  private final Solver solver;
  private static final Logger LOGGER = LoggerFactory.getLogger(OclProblemListener.class);

  @Inject
  public OclProblemListener(MessageInterface messageInterface, Solver solver) {
    this.messageInterface = messageInterface;
    this.solver = solver;
  }


  @Override
  public void run() {
    Subscription subscribe = messageInterface
        .subscribe(OclSolutionRequest.class, OclSolutionRequest.parser(),
            (id, solutionRequest) -> {

              String userId = solutionRequest.getUserId();

              OclCsp csp = OclCsp.ofConstraints(
                  solutionRequest.getProblem().getRequirementsList().stream().map(
                      OclRequirement::getConstraint).collect(
                      Collectors.toSet()));

              try {
                Solution solution = solver.solve(csp, userId);

                if (solution == null) {
                  messageInterface.reply(MatchmakingResponse.class, id,
                      Error.newBuilder().setCode(400)
                          .setMessage(
                              String.format("Could not find a solution for the problem %s.", csp))
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
                LOGGER.error(String.format("Error while solving the problem %s.", csp), e);
                messageInterface.reply(MatchmakingResponse.class, id,
                    Error.newBuilder().setCode(500)
                        .setMessage(
                            String
                                .format("An error occurred while solving the problem %s: %s", csp,
                                    e.getMessage()))
                        .build());
              }

            });
  }
}
