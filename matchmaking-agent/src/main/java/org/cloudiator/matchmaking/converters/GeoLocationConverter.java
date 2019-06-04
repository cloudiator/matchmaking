package org.cloudiator.matchmaking.converters;

import cloudiator.CloudiatorFactory;
import cloudiator.GeoLocation;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.GeoLocation.Builder;

public class GeoLocationConverter implements
    TwoWayConverter<IaasEntities.GeoLocation, GeoLocation> {

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorFactory.eINSTANCE;

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
