package org.cloudiator.matchmaking.choco;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import org.cloudiator.matchmaking.domain.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.matchmaking.experiment.ExperimentCSP;
import org.cloudiator.matchmaking.ocl.ConsistentNodeGenerator;
import org.cloudiator.matchmaking.ocl.ConstraintChecker;
import org.cloudiator.matchmaking.ocl.DefaultNodeGenerator;
import org.cloudiator.matchmaking.ocl.ModelGenerationException;
import org.cloudiator.matchmaking.ocl.ModelGenerator;
import org.cloudiator.matchmaking.ocl.NodeGenerator;
import org.cloudiator.matchmaking.ocl.OclContext;
import org.cloudiator.matchmaking.ocl.OclServiceModule;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

public class ChocoRunner {

  public static void main(String[] args) throws ModelGenerationException {

    Injector injector = Guice.createInjector(new MessageServiceModule(),
        new KafkaMessagingModule(new KafkaContext(Configuration.conf().getConfig("kafka"))),
        new OclServiceModule(
            new OclContext(Configuration.conf().getConfig("ocl"))));

    final ModelGenerator instance = injector.getInstance(ModelGenerator.class);

    NodeGenerator nodeGenerator = new ConsistentNodeGenerator(
        new DefaultNodeGenerator(NodeCandidateFactory.create(),
            instance.generateModel("dummy_user_id")),
        ConstraintChecker.create(ExperimentCSP.CSP));

    new ChocoSolver().solve(ExperimentCSP.CSP, nodeGenerator.get(), null);
  }

}
