package org.cloudiator.ocl;

import cloudiator.CloudiatorModel;
import com.google.inject.Inject;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.cloudiator.converters.NodeCandidateConverter;
import org.cloudiator.messages.NodeCandidate.NodeCandidateRequestMessage;
import org.cloudiator.messages.NodeCandidate.NodeCandidateRequestResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.cloudiator.ocl.NodeCandidate.NodeCandidateFactory;

public class OclNodeCandidateListener implements Runnable {

  private final MessageInterface messageInterface;
  private final ModelGenerator modelGenerator;
  private static final NodeCandidateConverter NODE_CANDIDATE_CONVERTER = new NodeCandidateConverter();

  @Inject
  public OclNodeCandidateListener(MessageInterface messageInterface,
      ModelGenerator modelGenerator) {
    this.messageInterface = messageInterface;
    this.modelGenerator = modelGenerator;
  }

  @Override
  public void run() {
    Subscription subscription = messageInterface.subscribe(NodeCandidateRequestMessage.class,
        NodeCandidateRequestMessage.parser(), (id, content) -> {
          final CloudiatorModel cloudiatorModel = modelGenerator.generateModel(id);
          final DefaultNodeGenerator nodeGenerator = new DefaultNodeGenerator(
              new NodeCandidateFactory(), cloudiatorModel);

          messageInterface
              .reply(id, NodeCandidateRequestResponse.newBuilder().addAllCandidates(
                  nodeGenerator.getPossibleNodes().stream().map(NODE_CANDIDATE_CONVERTER)
                      .collect(
                          Collectors.toList())).build());

        });
  }
}
