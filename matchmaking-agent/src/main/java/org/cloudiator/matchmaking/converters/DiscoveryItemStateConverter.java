package org.cloudiator.matchmaking.converters;

import cloudiator.DiscoveryItemState;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;


public class DiscoveryItemStateConverter implements
    TwoWayConverter<DiscoveryItemState, IaasEntities.DiscoveryItemState> {

  public static final DiscoveryItemStateConverter INSTANCE = new DiscoveryItemStateConverter();

  private DiscoveryItemStateConverter() {}

  @Override
  public DiscoveryItemState applyBack(IaasEntities.DiscoveryItemState discoveryItemState) {
    switch (discoveryItemState) {
      case DISCOVERY_DELETED:
        return DiscoveryItemState.DELETED;
      case DISCOVERY_REMOTELY_DELETED:
        return DiscoveryItemState.REMOTELY_DELETED;
      case DISCOVERY_LOCALLY_DELETED:
        return DiscoveryItemState.LOCALLY_DELETED;
      case DISCOVERY_DISABLED:
        return DiscoveryItemState.DISABLED;
      case DISCOVERY_UNKNOWN:
        return DiscoveryItemState.UNKOWN;
      case DISCOVERY_NEW:
        return DiscoveryItemState.NEW;
      case DISCOVERY_OK:
        return DiscoveryItemState.OK;
      case UNRECOGNIZED:
      default:
        throw new AssertionError("Unknown state " + discoveryItemState);
    }
  }

  @Override
  public IaasEntities.DiscoveryItemState apply(DiscoveryItemState discoveryItemState) {
    switch (discoveryItemState) {
      case OK:
        return IaasEntities.DiscoveryItemState.DISCOVERY_OK;
      case NEW:
        return IaasEntities.DiscoveryItemState.DISCOVERY_NEW;
      case DISABLED:
        return IaasEntities.DiscoveryItemState.DISCOVERY_DISABLED;
      case LOCALLY_DELETED:
        return IaasEntities.DiscoveryItemState.DISCOVERY_LOCALLY_DELETED;
      case REMOTELY_DELETED:
        return IaasEntities.DiscoveryItemState.DISCOVERY_REMOTELY_DELETED;
      case DELETED:
        return IaasEntities.DiscoveryItemState.DISCOVERY_DELETED;
      case UNKOWN:
        return IaasEntities.DiscoveryItemState.DISCOVERY_UNKNOWN;
      default:
        throw new AssertionError("Unknown state " + discoveryItemState);
    }
  }
}
