package org.cloudiator.matchmaking.experiment;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.cloudiator.matchmaking.ocl.MemoryCachedModelGenerator;
import org.cloudiator.matchmaking.ocl.ModelGenerator;
import org.cloudiator.matchmaking.ocl.PriceFunction;

public class ExperimentModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ModelGenerator.class).annotatedWith(Names.named("Base"))
        .to(LargeModelGenerator.class);
    bind(ModelGenerator.class).to(MemoryCachedModelGenerator.class);
    bind(PriceFunction.class).to(ExperimentModelPriceFunction.class);
    bindConstant().annotatedWith(Names.named("cacheTime"))
        .to(MemoryCachedModelGenerator.CACHE_INFINITE);
  }
}
