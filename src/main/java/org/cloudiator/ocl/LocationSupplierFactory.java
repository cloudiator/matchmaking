package org.cloudiator.ocl;

import cloudiator.Cloud;
import com.google.inject.Inject;
import org.cloudiator.messaging.services.LocationService;

public class LocationSupplierFactory {

  private final LocationService locationService;

  @Inject public LocationSupplierFactory(LocationService locationService) {
    this.locationService = locationService;
  }

  public LocationSupplier newInstance(Cloud cloud, String userId) {
    return new LocationSupplier(locationService, userId, cloud);
  }
}
