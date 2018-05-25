package org.cloudiator.matchmaking;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.cloudiator.matchmaking.ocl.MatchmakingRequestListener;
import org.cloudiator.matchmaking.ocl.NodeCandidateListener;
import org.cloudiator.matchmaking.ocl.OclContext;
import org.cloudiator.matchmaking.ocl.OclServiceModule;
import org.eclipse.ocl.pivot.utilities.ParserException;

public class MatchmakingAgent {

  private final static Injector injector = Guice
      .createInjector(new MessageServiceModule(),
          new OclServiceModule(new OclContext(Configuration.conf().getConfig("matchmaking"))),
          new KafkaMessagingModule(new KafkaContext(
              Configuration.conf())));

  public MatchmakingAgent() {

  }

  public static void main(String[] args) throws ParserException {
    injector.getInstance(MatchmakingRequestListener.class).run();
    injector.getInstance(NodeCandidateListener.class).run();
  }

}
