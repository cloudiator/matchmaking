package org.cloudiator.ocl;

import cloudiator.CloudiatorModel;

public interface ModelGenerator {

  CloudiatorModel generateModel(String userId);
}
