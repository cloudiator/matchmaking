package org.cloudiator.matchmaking;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import org.cloudiator.matchmaking.ocl.OclContext;
import org.cloudiator.matchmaking.ocl.OclServiceModule;
import org.cloudiator.messages.Cloud.QuotaQueryRequest;
import org.cloudiator.messages.Cloud.QuotaQueryResponse;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.CloudService;
import org.cloudiator.messaging.services.MessageServiceModule;

public class test {

  private final static Injector injector = Guice
      .createInjector(new MessageServiceModule(),
          new OclServiceModule(new OclContext(Configuration.conf().getConfig("matchmaking"))),
          new KafkaMessagingModule(new KafkaContext(
              Configuration.conf())));

  public static void main(String[] args) {
    final MessageInterface messageInterface = injector.getInstance(MessageInterface.class);

    final CloudService cloudService = injector.getInstance(CloudService.class);

    try {
      final QuotaQueryResponse admin = cloudService.queryQuota(
          QuotaQueryRequest.newBuilder().setUserId("admin").build());

      System.out.println(admin);


    } catch (ResponseException e) {
      e.printStackTrace();
    }
  }
}
