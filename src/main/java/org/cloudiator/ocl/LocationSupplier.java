package org.cloudiator.ocl;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import cloudiator.Location;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.cloudiator.messages.Location.LocationQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.LocationService;
import org.cloudiator.messaging.services.LocationServiceImpl;

public class LocationSupplier implements Supplier<Set<Location>> {

  private final CloudiatorFactory cloudiatorFactory = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private final LocationService locationService;
  private final String userId;
  private final Cloud cloud;

  public LocationSupplier(LocationService locationService, String userId, Cloud cloud) {
    this.locationService = locationService;
    this.userId = userId;
    this.cloud = cloud;
  }

  private LocationQueryRequest buildRequest() {
    return LocationQueryRequest.newBuilder().setCloudId(cloud.getId()).setUserId(userId).build();
  }

  @Override
  public Set<Location> get() {
    try {
      return locationService.getLocations(buildRequest()).getLocationsList().stream()
          .map(l -> {
            Location location = cloudiatorFactory.createLocation();
            location.setId(l.getId());
            location.setName(l.getName());
            location.setProviderId(l.getProviderId());
            if (l.hasGeoLocation()) {
              location.setCity(l.getGeoLocation().getCity());
              location.setCountry(l.getGeoLocation().getCountry());
              location.setLatitude(l.getGeoLocation().getLatitude());
            }
            return location;
          }).collect(
              Collectors.toSet());
    } catch (ResponseException e) {
      throw new IllegalStateException(
          String.format("Could not retrieve locations due to error %s", e.getMessage()), e);
    }
  }
}
