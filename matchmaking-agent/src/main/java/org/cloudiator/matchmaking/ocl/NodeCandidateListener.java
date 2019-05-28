package org.cloudiator.matchmaking.ocl;

import cloudiator.CloudiatorModel;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.converters.NodeCandidateConverter;
import org.cloudiator.matchmaking.converters.RequirementConverter;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.matchmaking.ocl.NodeCandidateCache.CachedNodeGenerator;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.entities.Matchmaking.NodeCandidateRequestMessage;
import org.cloudiator.messages.entities.Matchmaking.NodeCandidateRequestResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.Subscription;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeCandidateListener implements Runnable {

  private static final NodeCandidateConverter NODE_CANDIDATE_CONVERTER = NodeCandidateConverter.INSTANCE;
  private static final RequirementConverter REQUIREMENT_CONVERTER = RequirementConverter.INSTANCE;
  private final Logger LOGGER = LoggerFactory.getLogger(NodeCandidateListener.class);
  private final MessageInterface messageInterface;
  private final ModelGenerator modelGenerator;

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

          LOGGER.info(String.format("%s received new node candidate request %s.", this, content));

          try {
            final CloudiatorModel cloudiatorModel = modelGenerator
                .generateModel(content.getUserId());

            final CachedNodeGenerator cachedNodeGenerator = NodeCandidateCache
                .cache(content.getUserId(), new DefaultNodeGenerator(
                    NodeCandidateFactory.create(), cloudiatorModel));

            final NodeCandidates nodeCandidates;
            if (Strings.isNullOrEmpty(content.getId())) {
              OclCsp oclCsp = OclCsp
                  .ofRequirements(
                      content.getRequirementsList().stream()
                          .map(REQUIREMENT_CONVERTER).collect(Collectors.toList()), 1
                  );

              LOGGER.info(String.format("%s generated the csp %s.", this, oclCsp));

              final ConsistentNodeGenerator consistentNodeGenerator = new ConsistentNodeGenerator(
                  cachedNodeGenerator, ConstraintChecker.create(oclCsp));

              nodeCandidates = consistentNodeGenerator.get();
            } else {
              nodeCandidates = NodeCandidates.single(cachedNodeGenerator.get(content.getId()));
            }

            LOGGER.info(String.format("%s found %s nodes for request. Replying.", this,
                nodeCandidates.size()));

            messageInterface
                .reply(id, NodeCandidateRequestResponse.newBuilder().addAllCandidates(
                    nodeCandidates.stream()
                        .map(NODE_CANDIDATE_CONVERTER)
                        .collect(
                            Collectors.toList())).build());

          } catch (ParserException e) {
            LOGGER.error("Error while parsing constraint problem.", e);
            messageInterface.reply(NodeCandidateRequestResponse.class, id,
                Error.newBuilder().setCode(400).setMessage(String
                    .format("Could not parse constraint problem. Error was %s.", e.getMessage()))
                    .build());
          } catch (ModelGenerationException e) {
            LOGGER.error("Error while generating the model.", e);
            messageInterface.reply(NodeCandidateRequestResponse.class, id,
                Error.newBuilder().setCode(400).setMessage(String
                    .format("Could not generate model. Error was %s.", e.getMessage()))
                    .build());
          } catch (Exception e) {
            LOGGER.error("Unexpected error while calculating the node candidates.", e);
            messageInterface.reply(NodeCandidateRequestResponse.class, id,
                Error.newBuilder().setCode(500).setMessage(String
                    .format("Unexpected error. Error was %s.", e.getMessage()))
                    .build());
          }

        });
  }
}
