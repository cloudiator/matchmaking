package org.cloudiator.ocl;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage;
import com.google.inject.Inject;

public class ModelGeneratorImpl implements ModelGenerator {

  private final CloudiatorFactory cloudiatorFactory = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private final CloudModelGeneratorFactory cloudModelGeneratorFactory;


  @Inject
  public ModelGeneratorImpl(CloudModelGeneratorFactory cloudModelGeneratorFactory) {
    this.cloudModelGeneratorFactory = cloudModelGeneratorFactory;
  }

  @Override
  public CloudiatorModel generateModel(String userId) {
    final CloudiatorModel cloudiatorModel = cloudiatorFactory.createCloudiatorModel();
    cloudModelGeneratorFactory.newInstance(userId, cloudiatorModel).generate();
    return cloudiatorModel;
  }


}
