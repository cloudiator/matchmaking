package experiment;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.cloudiator.ocl.CachedModelGenerator;
import org.cloudiator.ocl.ModelGenerator;

public class ExperimentModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ModelGenerator.class).annotatedWith(Names.named("Base"))
        .to(ExperimentModelGenerator.class);
    bind(ModelGenerator.class).to(CachedModelGenerator.class);
  }
}
