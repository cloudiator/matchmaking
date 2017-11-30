package org.cloudiator.converters;

import cloudiator.CloudiatorFactory;
import cloudiator.Location;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import javax.annotation.Nullable;
import org.cloudiator.messages.entities.IaasEntities;

public class LocationConverter implements
    TwoWayConverter<IaasEntities.Location, Location> {

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorFactory.eINSTANCE;

  @Nullable
  @Override
  public IaasEntities.Location applyBack(@Nullable Location location) {
    return IaasEntities.Location.newBuilder().setProviderId(location.getProviderId())
        .setId(location.getId()).setGeoLocation(geoLocation(location)).build();
  }

  private IaasEntities.GeoLocation geoLocation(Location location) {
    return IaasEntities.GeoLocation.newBuilder().setCity(location.getCity())
        .setCountry(location.getCountry()).setLatitude(location.getLatitude())
        .setLongitude(location.getLongitude())
        .build();
  }

  @Override
  public Location apply(IaasEntities.Location location) {
    Location modelLocation = CLOUDIATOR_FACTORY.createLocation();
    modelLocation.setId(location.getId());
    modelLocation.setProviderId(location.getProviderId());
    modelLocation.setName(location.getName());

    if (location.hasGeoLocation()) {
      modelLocation.setCity(location.getGeoLocation().getCity());
      modelLocation.setCountry(location.getGeoLocation().getCountry());
      modelLocation.setLatitude(location.getGeoLocation().getLatitude());
      modelLocation.setLongitude(location.getGeoLocation().getLongitude());
    }
    return modelLocation;
  }
}
