package org.cloudiator.ocl;

import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.cloudiator.messages.entities.CommonEntities.OclRequirement;
import org.cloudiator.messages.entities.IaasEntities.VirtualMachineRequest;
import org.cloudiator.messages.entities.Solution.OclSolutionRequest;
import org.cloudiator.messages.entities.Solution.OclSolutionResponse;
import org.cloudiator.messages.entities.SolutionEntities.OclSolution;
import org.cloudiator.messages.entities.SolutionEntities.OclSolution.Builder;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;

public class OclProblemListener implements Runnable {

  private final MessageInterface messageInterface;
  private final Solver solver;

  @Inject
  public OclProblemListener(MessageInterface messageInterface, Solver solver) {
    this.messageInterface = messageInterface;
    this.solver = solver;
  }


  @Override
  public void run() {
    Subscription subscribe = messageInterface
        .subscribe(OclSolutionRequest.class, OclSolutionRequest.parser(),
            new MessageCallback<OclSolutionRequest>() {
              @Override
              public void accept(String id, OclSolutionRequest solutionRequest) {

                String userId = solutionRequest.getUserId();

                ConstraintSatisfactionProblem csp = new ConstraintSatisfactionProblem(
                    solutionRequest.getProblem().getRequirementsList().stream().map(
                        OclRequirement::getConstraint).collect(
                        Collectors.toSet()));

                try {
                  Solution solution = solver.solve(csp, userId);

                  Builder oclSolutionBuilder = OclSolution.newBuilder();

                  solution.getList().forEach(new Consumer<NodeCandidate>() {
                    @Override
                    public void accept(NodeCandidate nodeCandidate) {
                      oclSolutionBuilder.addNodes(VirtualMachineRequest.newBuilder()
                          .setHardware(nodeCandidate.getHardware().getId())
                          .setImage(nodeCandidate.getImage().getId())
                          .setLocation(nodeCandidate.getLocation().getId()).build());
                    }
                  });

                  OclSolutionResponse solutionResponse = OclSolutionResponse.newBuilder()
                      .setSolution(oclSolutionBuilder.build())
                      .build();

                  messageInterface.reply(id, solutionResponse);

                } catch (Exception e) {
                  e.printStackTrace();
                }

              }
            });
  }
}
