package org.cloudiator.matchmaking.converters;

import cloudiator.CloudiatorFactory;
import cloudiator.GeoLocation;
import cloudiator.Location;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import javax.annotation.Nullable;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.GeoLocation.Builder;

public class LocationConverter implements
    TwoWayConverter<IaasEntities.Location, Location> {

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorFactory.eINSTANCE;
  private static final GeoLocationConverter GEO_LOCATION_CONVERTER = new GeoLocationConverter();
  private static final LocationScopeConverter LOCATION_SCOPE_CONVERTER = LocationScopeConverter.INSTANCE;
  private static final DiscoveryItemStateConverter DISCOVERY_ITEM_STATE_CONVERTER = DiscoveryItemStateConverter.INSTANCE;

  @Nullable
  @Override
  public IaasEntities.Location applyBack(@Nullable Location location) {
    if (location == null) {
      return null;
    }

    final IaasEntities.Location.Builder builder = IaasEntities.Location.newBuilder()
        .setProviderId(location.getProviderId())
        .setId(location.getId()).setName(location.getName())
        .setIsAssignable(location.isAssignable())
        .setLocationScope(LOCATION_SCOPE_CONVERTER.applyBack(location.getLocationScope()))
        .setState(DISCOVERY_ITEM_STATE_CONVERTER.apply(location.getState()))
        .setUserId(location.getOwner());

    if (location.getParent() != null) {
      builder.setParent(applyBack(location.getParent()));
    }

    if (location.getGeoLocation() != null) {
      builder.setGeoLocation(GEO_LOCATION_CONVERTER.applyBack(location.getGeoLocation()));
    }

    return builder.build();
  }

  @Override
  public Location apply(IaasEntities.Location location) {
    if (location == null) {
      return null;
    }

    Location modelLocation = CLOUDIATOR_FACTORY.createLocation();
    modelLocation.setId(location.getId());
    modelLocation.setProviderId(location.getProviderId());
    modelLocation.setName(location.getName());
    modelLocation.setAssignable(location.getIsAssignable());
    modelLocation.setLocationScope(LOCATION_SCOPE_CONVERTER.apply(location.getLocationScope()));
    modelLocation.setState(DISCOVERY_ITEM_STATE_CONVERTER.applyBack(location.getState()));
    modelLocation.setOwner(location.getUserId());

    if (location.hasParent()) {
      modelLocation.setParent(apply(location.getParent()));
    }

    if (location.hasGeoLocation()) {
      modelLocation.setGeoLocation(GEO_LOCATION_CONVERTER.apply(location.getGeoLocation()));
    }
    return modelLocation;
  }
}
