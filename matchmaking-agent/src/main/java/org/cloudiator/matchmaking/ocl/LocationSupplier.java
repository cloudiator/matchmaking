package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.Location;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.converters.LocationConverter;
import org.cloudiator.messages.Location.LocationQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.LocationService;

public class LocationSupplier implements Supplier<Set<Location>> {

  private static final LocationConverter LOCATION_CONVERTER = new LocationConverter();
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
          .map(LOCATION_CONVERTER).collect(
              Collectors.toSet());
    } catch (ResponseException e) {
      throw new IllegalStateException(
          String.format("Could not retrieve locations due to error %s", e.getMessage()), e);
    }
  }
}
