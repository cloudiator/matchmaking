package org.cloudiator.ocl;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class OclServiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ModelGenerator.class).annotatedWith(Names.named("Base"))
        .to(DiscoveryBasedModelGenerator.class);
    bind(ModelGenerator.class).to(CachedModelGenerator.class);
    bind(PriceFunction.class).to(HardwareBasedPriceFunction.class);
  }
}
