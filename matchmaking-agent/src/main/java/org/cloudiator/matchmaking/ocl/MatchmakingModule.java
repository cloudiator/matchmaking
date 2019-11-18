package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import org.cloudiator.matchmaking.domain.Solver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatchmakingModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(MatchmakingModule.class);
  private final OclContext oclContext;

  public MatchmakingModule(OclContext oclContext) {
    checkNotNull(oclContext, "oclContext is null");
    this.oclContext = oclContext;
  }

  @Override
  protected void configure() {

    Multibinder<Solver> solverBinder = Multibinder.newSetBinder(binder(), Solver.class);

    for (String clazz : oclContext.solvers()) {

      try {
        Class<? extends Solver> solverClass = Class.forName(clazz).asSubclass(Solver.class);
        solverBinder.addBinding().to(solverClass);
        LOGGER.info(String.format("%s is loading the solver %s", this, solverClass));
      } catch (ClassNotFoundException e) {
        throw new IllegalStateException(String.format("Could not find solver with name %s.", clazz),
            e);
      } catch (ClassCastException e) {
        throw new IllegalStateException(
            String.format("Class %s does not implement Solver interface.", clazz), e);
      }


    }

    //expire binder
    Multibinder<Expirable> expireBinder = Multibinder
        .newSetBinder(binder(), Expirable.class);
    expireBinder.addBinding().to(SolutionCacheImpl.class);
    expireBinder.addBinding().to(NodeCandidateCache.class);

    Multibinder<PriceFunction> priceFunctionMultibinder = Multibinder
        .newSetBinder(binder(), PriceFunction.class);
    priceFunctionMultibinder.addBinding().to(CSPSourcedPricePlanPriceFunction.class);
    priceFunctionMultibinder.addBinding().to(HardwareBasedPriceFunction.class);

    bind(PriceFunction.class).to(CompositePriceFunction.class).in(Scopes.SINGLETON);

    LOGGER.info(String.format("Using %s as model generator.",
        oclContext.modelGenerator().modelGeneratorClass().getName()));
    bind(ModelGenerator.class).annotatedWith(Names.named("Base"))
        .to(oclContext.modelGenerator().modelGeneratorClass());

    bindConstant().annotatedWith(Names.named("cacheTime")).to(oclContext.cacheTime());

    bindConstant().annotatedWith(Names.named("solvingTime")).to(oclContext.solvingTime());

    bindConstant().annotatedWith(Names.named("considerQuota")).to(oclContext.considerQuotas());

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

    bind(ByonGenerator.class).asEagerSingleton();
  }
}
