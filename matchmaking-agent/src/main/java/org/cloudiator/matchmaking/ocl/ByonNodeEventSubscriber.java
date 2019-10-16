/*
 * Copyright 2014-2019 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudiator.matchmaking.ocl;

import com.google.inject.Inject;
import org.cloudiator.messages.Byon;
import org.cloudiator.messages.entities.ByonEntities;
import org.cloudiator.messages.entities.ByonEntities.CacheOperation;
import org.cloudiator.messaging.services.ByonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByonNodeEventSubscriber implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ByonNodeEventSubscriber.class);
  private final ByonService byonService;
  private final ByonCache cache;

  @Inject
  public ByonNodeEventSubscriber(ByonService byonService,
      ByonCache cache) {
    this.byonService = byonService;
    this.cache = cache;
  }

  @Override
  public void run() {
    byonService.subscribeByonNodeEvents((id, content) -> {

      Byon.ByonNode messageByonNode = content.getByonNode();
      ByonEntities.CacheOperation operation = content.getOperation();

      doManipulateCache(messageByonNode, operation);
    });
  }

  private void doManipulateCache(Byon.ByonNode byonNode, CacheOperation operation) {

    switch (operation) {

      case ADD:
        if (byonNode.getNodeData().getAllocated()) {
          LOGGER.error(String.format("Cannot add byon with id: %s to cache as it is in"
              + "inconsistent state: %s", byonNode.getId(), byonNode.getNodeData().getAllocated()));
        }
        cache.add(byonNode);
        break;
      case EVICT:
        if (byonNode.getNodeData().getAllocated()) {
          LOGGER.error(String.format("Cannot evict byon with id: %s from cache as it is in"
              + "inconsistent state: %s", byonNode.getId(), byonNode.getNodeData().getAllocated()));
        }
        cache.evict(byonNode.getId(), byonNode.getUserId());
        break;
      case UPDATE:
        if (byonNode.getNodeData().getAllocated()) {
          cache.evict(byonNode.getId(), byonNode.getUserId());
        } else {
          cache.add(byonNode);
        }
        break;
      default:
        throw new IllegalArgumentException(
            String.format("Unknown operation: %s for manipulating the cache.", operation));
    }
  }
}
