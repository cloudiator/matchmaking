package org.cloudiator.converters;

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

  @Nullable
  @Override
  public IaasEntities.Location applyBack(@Nullable Location location) {
    final IaasEntities.Location.Builder builder = IaasEntities.Location.newBuilder()
        .setProviderId(location.getProviderId())
        .setId(location.getId()).setName(location.getName());

    if (location.getGeoLocation() != null) {
      builder.setGeoLocation(GEO_LOCATION_CONVERTER.applyBack(location.getGeoLocation()));
    }

    return builder.build();
  }

  @Override
  public Location apply(IaasEntities.Location location) {
    Location modelLocation = CLOUDIATOR_FACTORY.createLocation();
    modelLocation.setId(location.getId());
    modelLocation.setProviderId(location.getProviderId());
    modelLocation.setName(location.getName());

    if (location.hasGeoLocation()) {
      modelLocation.setGeoLocation(GEO_LOCATION_CONVERTER.apply(location.getGeoLocation()));
    }
    return modelLocation;
  }

  private static class GeoLocationConverter implements
      TwoWayConverter<IaasEntities.GeoLocation, GeoLocation> {

    @Override
    public IaasEntities.GeoLocation applyBack(GeoLocation geoLocation) {
      final Builder builder = IaasEntities.GeoLocation.newBuilder();

      if (geoLocation.getCity() != null) {
        builder.setCity(geoLocation.getCity());
      }

      if (geoLocation.getCountry() != null) {
        builder.setCountry(geoLocation.getCountry());
      }

      if (geoLocation.getLatitude() != null) {
        builder.setLatitude(geoLocation.getLatitude());
      }

      if (geoLocation.getLongitude() != null) {
        builder.setLongitude(geoLocation.getLongitude());
      }

      return builder.build();
    }

    @Override
    public GeoLocation apply(IaasEntities.GeoLocation geoLocation) {
      GeoLocation modelLocation = CLOUDIATOR_FACTORY.createGeoLocation();
      modelLocation.setCity(geoLocation.getCity());
      modelLocation.setCountry(geoLocation.getCountry());
      modelLocation.setLatitude(geoLocation.getLatitude());
      modelLocation.setLongitude(geoLocation.getLongitude());
      return modelLocation;
    }
  }

}
