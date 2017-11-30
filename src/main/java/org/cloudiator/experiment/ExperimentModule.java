package org.cloudiator.experiment;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.cloudiator.ocl.MemoryCachedModelGenerator;
import org.cloudiator.ocl.ModelGenerator;
import org.cloudiator.ocl.PriceFunction;

public class ExperimentModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ModelGenerator.class).annotatedWith(Names.named("Base"))
        .to(LargeModelGenerator.class);
    bind(ModelGenerator.class).to(MemoryCachedModelGenerator.class);
    bind(PriceFunction.class).to(ExperimentModelPriceFunction.class);
  }
}
