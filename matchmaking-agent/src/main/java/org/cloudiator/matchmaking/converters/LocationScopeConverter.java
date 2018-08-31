package org.cloudiator.matchmaking.converters;

import cloudiator.LocationScope;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.CommonEntities;

public class LocationScopeConverter implements
    TwoWayConverter<CommonEntities.LocationScope, LocationScope> {

  public static final LocationScopeConverter INSTANCE = new LocationScopeConverter();

  private LocationScopeConverter() {}

  @Override
  public CommonEntities.LocationScope applyBack(LocationScope locationScope) {
    switch (locationScope) {
      case HOST:
        return CommonEntities.LocationScope.HOST;
      case ZONE:
        return CommonEntities.LocationScope.ZONE;
      case REGION:
        return CommonEntities.LocationScope.REGION;
      case PROVIDER:
        return CommonEntities.LocationScope.PROVIDER;
      case UNKOWN:
      default:
        throw new AssertionError("Illegal locationScope " + locationScope);
    }
  }

  @Override
  public LocationScope apply(CommonEntities.LocationScope locationScope) {
    switch (locationScope) {
      case PROVIDER:
        return LocationScope.PROVIDER;
      case REGION:
        return LocationScope.REGION;
      case ZONE:
        return LocationScope.ZONE;
      case HOST:
        return LocationScope.HOST;
      case UNRECOGNIZED:
      default:
        throw new AssertionError("Illegal locationScope " + locationScope);
    }
  }
}
