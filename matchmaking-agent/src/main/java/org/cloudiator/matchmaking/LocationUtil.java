package org.cloudiator.matchmaking;

import static com.google.common.base.Preconditions.checkNotNull;

import cloudiator.Cloud;
import cloudiator.CloudiatorModel;
import cloudiator.Location;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class LocationUtil {


  public static class LocationIterable implements Iterable<Location> {

    private final Location location;

    public LocationIterable(Location location) {
      this.location = location;
    }

    @Override
    public Iterator<Location> iterator() {
      return new LocationIterator(location);
    }

    public static class LocationIterator implements Iterator<Location> {

      @Nullable
      private Location cursor;

      public LocationIterator(Location start) {
        checkNotNull(start, "start is null");
        this.cursor = start;
      }

      @Override
      public boolean hasNext() {
        return cursor != null;
      }

      @Override
      public Location next() {
        if (cursor == null) {
          throw new NoSuchElementException();
        }
        Location current = cursor;
        cursor = current.getParent();
        return current;
      }
    }

  }

  public static Set<Location> parents(Location location) {
    Set<Location> locations = new HashSet<>();
    final Iterator<Location> iterator = new LocationIterable(location).iterator();
    while (iterator.hasNext()) {
      locations.add(iterator.next());
    }
    return locations;
  }

  public static Optional<Location> findLocation(String id, CloudiatorModel cloudiatorModel) {
    return cloudiatorModel.getClouds().stream().flatMap(
        (Function<Cloud, Stream<Location>>) cloud -> cloud.getLocations().stream()).filter(
        location -> location.getId().equals(id)).collect(StreamUtil.getOnly());
  }

  public static boolean inHierarchy(String id, Location location) {
    return parents(location).stream().anyMatch(l -> l.getId().equals(id));
  }

  public static Set<Location> subLocations(Location location, CloudiatorModel cloudiatorModel) {

    final Set<Location> allLocations = cloudiatorModel.getClouds()
        .stream()
        .flatMap(
            (Function<Cloud, Stream<Location>>) cloud -> cloud.getLocations().stream())
        .collect(Collectors.toSet());

    final Set<Location> subLocations = new HashSet<>();

    for (Location inModel : allLocations) {
      for (Location current = inModel; current != null; current = current.getParent()) {
        if (location.equals(current.getParent())) {
          subLocations.add(current);
        }
      }
    }

    return subLocations;
  }


}
