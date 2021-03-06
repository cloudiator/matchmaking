package org.cloudiator.matchmaking.ocl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import java.util.List;

public class OclContext {

  private final Config config;

  public OclContext() {
    this(Configuration.conf());
  }

  public OclContext(Config config) {
    this.config = config;
    config.checkValid(ConfigFactory.defaultReference(), "ocl");
  }

  public ModelGenerators modelGenerator() {
    return ModelGenerators.valueOf(config.getString("discoveryModel"));
  }

  public List<String> solvers() {
    return config.getStringList("solvers");
  }

  public int cacheTime() {
    return config.getInt("cacheTime");
  }

  public int solvingTime() {
    return config.getInt("solvingTime");
  }

  public boolean considerQuotas() {
    return config.getBoolean("considerQuota");
  }

}
