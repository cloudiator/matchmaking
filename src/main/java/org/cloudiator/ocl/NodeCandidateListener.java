package org.cloudiator.ocl;

import cloudiator.CloudiatorModel;
import com.google.inject.Inject;
import java.util.stream.Collectors;
import org.cloudiator.converters.NodeCandidateConverter;
import org.cloudiator.converters.RequirementConverter;
import org.cloudiator.domain.RepresentableAsOCL;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.entities.Matchmaking.NodeCandidateRequestMessage;
import org.cloudiator.messages.entities.Matchmaking.NodeCandidateRequestResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.ocl.NodeCandidate.NodeCandidateFactory;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeCandidateListener implements Runnable {

  private final Logger LOGGER = LoggerFactory.getLogger(NodeCandidateListener.class);
  private final MessageInterface messageInterface;
  private final ModelGenerator modelGenerator;
  private static final NodeCandidateConverter NODE_CANDIDATE_CONVERTER = new NodeCandidateConverter();
  private static final RequirementConverter REQUIREMENT_CONVERTER = new RequirementConverter();

  @Inject
  public NodeCandidateListener(MessageInterface messageInterface,
      ModelGenerator modelGenerator) {
    this.messageInterface = messageInterface;
    this.modelGenerator = modelGenerator;
  }

  @Override
  public void run() {
    Subscription subscription = messageInterface.subscribe(NodeCandidateRequestMessage.class,
        NodeCandidateRequestMessage.parser(), (id, content) -> {
          final CloudiatorModel cloudiatorModel = modelGenerator.generateModel(id);
          final DefaultNodeGenerator defaultNodeGenerator = new DefaultNodeGenerator(
              new NodeCandidateFactory(), cloudiatorModel);

          OclCsp oclCsp = OclCsp
              .ofRequirements(REQUIREMENT_CONVERTER.apply(content.getRequirements()).stream().map(
                  requirement -> (RepresentableAsOCL) requirement).collect(Collectors.toList()));

          try {
            final ConsistentNodeGenerator consistentNodeGenerator = new ConsistentNodeGenerator(
                defaultNodeGenerator, new ConstraintChecker(oclCsp));

            messageInterface
                .reply(id, NodeCandidateRequestResponse.newBuilder().addAllCandidates(
                    consistentNodeGenerator.getPossibleNodes().stream()
                        .map(NODE_CANDIDATE_CONVERTER)
                        .collect(
                            Collectors.toList())).build());

          } catch (ParserException e) {
            LOGGER.error("Error while parsing constraint problem.", e);
            messageInterface.reply(NodeCandidateRequestResponse.class, id,
                Error.newBuilder().setCode(400).setMessage(String
                    .format("Could not parse constraint problem. Error was %s.", e.getMessage()))
                    .build());
          }

        });
  }
}
