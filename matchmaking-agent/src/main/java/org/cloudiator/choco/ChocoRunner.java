package org.cloudiator.choco;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import org.cloudiator.experiment.ExperimentCSP;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;
import org.cloudiator.ocl.ConsistentNodeGenerator;
import org.cloudiator.ocl.ConstraintChecker;
import org.cloudiator.ocl.DefaultNodeGenerator;
import org.cloudiator.ocl.ModelGenerationException;
import org.cloudiator.ocl.ModelGenerator;
import org.cloudiator.ocl.NodeCandidate.NodeCandidateFactory;
import org.cloudiator.ocl.NodeGenerator;
import org.cloudiator.ocl.OclContext;
import org.cloudiator.ocl.OclServiceModule;

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

    new ChocoSolver().solve(ExperimentCSP.CSP, nodeGenerator.getPossibleNodes());
  }

}
