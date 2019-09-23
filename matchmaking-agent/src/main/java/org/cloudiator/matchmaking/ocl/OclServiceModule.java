package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.cloudiator.matchmaking.choco.ChocoSolver;
import org.cloudiator.matchmaking.domain.Solver;
import org.cloudiator.messaging.services.PricingService;
import org.cloudiator.messaging.services.PricingServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OclServiceModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(OclServiceModule.class);
  private final OclContext oclContext;

  public OclServiceModule(OclContext oclContext) {
    checkNotNull(oclContext, "oclContext is null");
    this.oclContext = oclContext;
  }

  @Override
  protected void configure() {

    Multibinder<Solver> solverBinder = Multibinder.newSetBinder(binder(), Solver.class);
    solverBinder.addBinding().to(ChocoSolver.class);
    solverBinder.addBinding().to(BestFitSolver.class);

    //expire binder
    Multibinder<Expirable> expireBinder = Multibinder
        .newSetBinder(binder(), Expirable.class);
    expireBinder.addBinding().to(SolutionCacheImpl.class);
    expireBinder.addBinding().to(NodeCandidateCache.class);

    //bind(PriceFunction.class).to(HardwareBasedPriceFunction.class);
    bind(PriceFunction.class).to(CSPSourcedPricePlanPriceFunction.class);

    LOGGER.info(String.format("Using %s as model generator.",
        oclContext.modelGenerator().modelGeneratorClass().getName()));
    bind(ModelGenerator.class).annotatedWith(Names.named("Base"))
        .to(oclContext.modelGenerator().modelGeneratorClass());

    bindConstant().annotatedWith(Names.named("cacheTime")).to(oclContext.cacheTime());

    bindConstant().annotatedWith(Names.named("solvingTime")).to(oclContext.solvingTime());

    if (oclContext.modelGenerator().cacheClass().isPresent()) {
      LOGGER.info(String.format("Using cache %s for model generator.",
          oclContext.modelGenerator().cacheClass().get().getName()));
      bind(ModelGenerator.class).to(oclContext.modelGenerator().cacheClass().get());
      if (Expirable.class.isAssignableFrom(oclContext.modelGenerator().cacheClass().get())) {
        //noinspection unchecked
        expireBinder.addBinding().to(
            (Class<? extends Expirable>) oclContext.modelGenerator().cacheClass().get());
      }
    } else {
      bind(ModelGenerator.class).to(oclContext.modelGenerator().modelGeneratorClass());
    }
  }
}
