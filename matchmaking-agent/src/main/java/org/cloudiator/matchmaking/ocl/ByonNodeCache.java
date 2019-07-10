package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkNotNull;

import cloudiator.NodeType;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.uniulm.omi.cloudiator.sword.domain.OfferQuota.OfferType;
import de.uniulm.omi.cloudiator.sword.domain.Quota;
import de.uniulm.omi.cloudiator.sword.domain.Quotas;
import io.github.cloudiator.domain.ByonIdCreator;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.converters.GeoLocationConverter;
import org.cloudiator.matchmaking.converters.OperatingSystemConverter;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.messages.Byon.ByonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class ByonNodeCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(ByonNodeCache.class);
  private static final OperatingSystemConverter OS_CONVERTER = new OperatingSystemConverter();
  private static final GeoLocationConverter GEO_LOCATION_CONVERTER = new GeoLocationConverter();
  private final ByonUpdater updater;
  private final Set<Expirable> expirableSet;
  private volatile Map<ByonCacheKey, ByonNode> byonNodeCache = new HashMap<>();
  private final Cache<ByonCacheKey, ByonNode> tempCache =
      CacheBuilder.newBuilder()
          .expireAfterWrite(5, TimeUnit.MINUTES)
          .removalListener(
              new RemovalListener<ByonCacheKey, ByonNode>() {
                @Override
                public void onRemoval(
                    RemovalNotification<ByonCacheKey, ByonNode> removalNotification) {
                  add(removalNotification.getValue());
                }
              })
          .build();

  @Inject
  public ByonNodeCache(ByonUpdater updater, Set<Expirable> expirableSet) {
    this.updater = updater;
    this.expirableSet = expirableSet;
  }

  public synchronized Optional<ByonNode> add(ByonNode node) {
    ByonCacheKey key = new ByonCacheKey(node.getId(), node.getUserId());
    if (hit(key.getNodeId(), key.getUserId()).isPresent()) {
      LOGGER.info(String.format("Overriding node with key %s in cache %s.", key, this));
    }

    ByonNode origByonNode = byonNodeCache.put(key, node);
    publishUpdate();
    // invalidate caches
    expirableSet.stream().forEach(expirable -> expirable.expire(key.getUserId()));

    return Optional.ofNullable(origByonNode);
  }

  public synchronized void evictBySolution(Solution solution, String userId) {
    io.github.cloudiator.messaging.OperatingSystemConverter OS_DOMAIN_CONVERTER =
        new io.github.cloudiator.messaging.OperatingSystemConverter();
    io.github.cloudiator.messaging.GeoLocationMessageToGeoLocationConverter GEO_LOC_DOMAIN_CONVERTER =
        new io.github.cloudiator.messaging.GeoLocationMessageToGeoLocationConverter();

    for (NodeCandidate nodeCandidate : solution.getNodeCandidates()) {
      if (nodeCandidate.getType().equals(NodeType.BYON)) {
        String id = ByonIdCreator.createId(nodeCandidate.getHardware().getCores(),
            nodeCandidate.getHardware().getRam(), nodeCandidate.getHardware().getDisk(),
            OS_DOMAIN_CONVERTER.apply(OS_CONVERTER.applyBack(nodeCandidate.getImage().getOperatingSystem())),
            GEO_LOC_DOMAIN_CONVERTER.apply(GEO_LOCATION_CONVERTER.applyBack(nodeCandidate.getLocation().getGeoLocation())));
        evictTemp(id, userId);
      }
    }
  }

  public synchronized Optional<ByonNode> evictTemp(String id, String userId) {
    Optional<ByonNode> evict = evict(id, userId);
    if (evict.isPresent()) {
      tempCache.put(new ByonCacheKey(id, userId), evict.get());
    }
    return evict;
  }

  public synchronized Optional<ByonNode> evict(String id, String userId) {
    ByonCacheKey key = new ByonCacheKey(id, userId);
    boolean syncProblemPresent = checkForSyncProblems(key);

    if (syncProblemPresent) {
      LOGGER.error(String.format("%s could not consistently evict byon with id %s from "
          + "cache as there were sync problems", this, id));
      return null;
    }

    // if in temp remove & insert record back into byonCache
    tempCache.invalidate(key);

    if (!hit(key.getNodeId(), key.getUserId()).isPresent()) {
      LOGGER.error(
          String.format(
              "Cannot evict node with key %s in cache %s as key is not present", key, this));
      return Optional.empty();
    }

    ByonNode evictNode = byonNodeCache.get(key);
    byonNodeCache.remove(key);
    publishUpdate();
    // invalidate caches
    expirableSet.stream().forEach(expirable -> expirable.expire(key.getUserId()));

    return Optional.of(evictNode);
  }

  private synchronized boolean checkForSyncProblems(ByonCacheKey key) {
    ByonNode inTmpCacheNode = tempCache.getIfPresent(key);
    ByonNode inByonCacheNode = byonNodeCache.get(key);

    // Correct state
    if (inTmpCacheNode != null && inByonCacheNode == null) {
      return false;
    }

    if (inTmpCacheNode == null) {
      if (inByonCacheNode == null) {
        LOGGER.error(String.format("Byon-Node with id %s that was assigned to "
            + "a solution seems not to got cached in the temp-cache formerly", key.nodeId));
      }
      if (inByonCacheNode != null) {
        LOGGER.error(String.format("Severe problem encountered. Byon-Node with id %s that was assigned to "
            + "a solution resides in byon-cache but not in temp-cache. Correct state would be the other"
            + "way around.", key.nodeId));
      }
    }

    if (inTmpCacheNode != null && inByonCacheNode != null) {
      LOGGER.error(String.format("Byon-Node with id %s that was assigned to "
          + "a solution seems not to got evicted temporarily from the byon-cache.", key.nodeId));
    }

    return true;
  }

  private synchronized void publishUpdate() {
    updater.update(this);
  }

  public synchronized Optional<ByonNode> hit(String id, String userId) {
    ByonCacheKey key = new ByonCacheKey(id, userId);
    return (byonNodeCache.get(key) == null)
        ? Optional.empty()
        : Optional.of(byonNodeCache.get(key));
  }

  public synchronized Optional<ByonNode> read(String id, String userId) {
    ByonCacheKey key = new ByonCacheKey(id, userId);
    if (!hit(key.getNodeId(), key.getUserId()).isPresent()) {
      LOGGER.error(
          String.format(
              "Cannot read node with key %s in cache %s as key is not present", key, this));
      return Optional.empty();
    }


    return Optional.of(byonNodeCache.get(key));
  }

  public synchronized Set<ByonNode> readAll() {
    return byonNodeCache.values().stream().collect(Collectors.toSet());
  }

  public synchronized Set<Quota> readAllCorrespondingQuotas() {
    return byonNodeCache
        .entrySet()
        .stream()
        .map(Map.Entry::getValue)
        .map(s -> Quotas.offerQuota(s.getId(), OfferType.HARDWARE, BigDecimal.valueOf(1), null))
        .collect(Collectors.toSet());
  }

  private static class ByonCacheKey {

    private final String nodeId;
    private final String userId;

    private ByonCacheKey(String nodeId, String userId) {
      checkNotNull(nodeId, "nodeId is null");
      checkNotNull(userId, "userId is null");
      this.nodeId = nodeId;
      this.userId = userId;
    }

    public String getNodeId() {
      return nodeId;
    }

    public String getUserId() {
      return userId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ByonCacheKey cacheKey = (ByonCacheKey) o;
      return nodeId.equals(cacheKey.nodeId) && userId.equals(cacheKey.userId);
    }

    @Override
    public String toString() {
      return String.format("(%s,%s)", nodeId, userId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(nodeId, userId);
    }
  }
}
