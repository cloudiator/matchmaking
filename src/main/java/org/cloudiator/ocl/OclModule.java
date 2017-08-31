package org.cloudiator.ocl;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class OclModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ModelGenerator.class).annotatedWith(Names.named("Base")).to(ModelGeneratorImpl.class);
    bind(ModelGenerator.class).to(CachedModelGenerator.class);

  }
}
