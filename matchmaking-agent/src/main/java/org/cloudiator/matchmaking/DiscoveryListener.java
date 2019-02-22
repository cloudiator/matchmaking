package org.cloudiator.matchmaking;

import com.google.inject.Inject;
import java.util.Set;
import org.cloudiator.messages.Cloud.CloudEvent;
import org.cloudiator.messages.Discovery.DiscoveryEvent;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryListener implements Runnable {

  private final MessageInterface messageInterface;
  private final Set<Expirable> expirableSet;
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryListener.class);

  @Inject
  public DiscoveryListener(MessageInterface messageInterface,
      Set<Expirable> expirableSet) {
    this.messageInterface = messageInterface;

    LOGGER.info(String.format("%s is handling the expiration of %s.", this, expirableSet));

    this.expirableSet = expirableSet;
  }

  private void expireForUser(String userId) {

    LOGGER.debug(String.format("Expiring everything for user %s.", userId));

    for (Expirable expirable : expirableSet) {
      expirable.expire(userId);
    }
  }

  @Override
  public void run() {

    messageInterface.subscribe(DiscoveryEvent.class, DiscoveryEvent.parser(),
        new MessageCallback<DiscoveryEvent>() {
          @Override
          public void accept(String id, DiscoveryEvent content) {
            expireForUser(content.getUserId());
          }
        });

    messageInterface.subscribe(CloudEvent.class, CloudEvent.parser(),
        new MessageCallback<CloudEvent>() {
          @Override
          public void accept(String id, CloudEvent content) {
            expireForUser(content.getUserId());
          }
        });


  }
}
