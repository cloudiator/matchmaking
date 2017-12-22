package org.cloudiator.ocl;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage;
import com.google.inject.Inject;

public class DiscoveryBasedModelGenerator implements ModelGenerator {

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private final DiscoveryServiceBasedCloudModelGeneratorFactory discoveryServiceBasedCloudModelGeneratorFactory;


  @Inject
  public DiscoveryBasedModelGenerator(
      DiscoveryServiceBasedCloudModelGeneratorFactory discoveryServiceBasedCloudModelGeneratorFactory) {
    this.discoveryServiceBasedCloudModelGeneratorFactory = discoveryServiceBasedCloudModelGeneratorFactory;
  }

  @Override
  public CloudiatorModel generateModel(String userId) throws ModelGenerationException {
    final CloudiatorModel cloudiatorModel = CLOUDIATOR_FACTORY.createCloudiatorModel();
    discoveryServiceBasedCloudModelGeneratorFactory.newInstance(userId, cloudiatorModel).generate();
    return cloudiatorModel;
  }


}
