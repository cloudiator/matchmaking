package org.cloudiator.ocl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OclServiceModule extends AbstractModule {

  private final OclContext oclContext;
  private static final Logger LOGGER = LoggerFactory.getLogger(OclServiceModule.class);

  public OclServiceModule(OclContext oclContext) {
    checkNotNull(oclContext, "oclContext is null");
    this.oclContext = oclContext;
  }

  @Override
  protected void configure() {
    bind(PriceFunction.class).to(HardwareBasedPriceFunction.class);

    LOGGER.info(String.format("Using %s as model generator.",
        oclContext.modelGenerator().modelGeneratorClass().getName()));
    bind(ModelGenerator.class).annotatedWith(Names.named("Base"))
        .to(oclContext.modelGenerator().modelGeneratorClass());

    if (oclContext.modelGenerator().cacheClass().isPresent()) {
      LOGGER.info(String.format("Using cache %s for model generator.",
          oclContext.modelGenerator().cacheClass().get().getName()));
      bind(ModelGenerator.class).to(oclContext.modelGenerator().cacheClass().get());
    } else {
      bind(ModelGenerator.class).to(oclContext.modelGenerator().modelGeneratorClass());
    }
  }
}
