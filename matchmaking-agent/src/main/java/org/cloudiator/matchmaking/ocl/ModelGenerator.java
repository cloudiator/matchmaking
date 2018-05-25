package org.cloudiator.matchmaking.ocl;

import cloudiator.CloudiatorModel;

public interface ModelGenerator {

  CloudiatorModel generateModel(String userId) throws ModelGenerationException;
}
