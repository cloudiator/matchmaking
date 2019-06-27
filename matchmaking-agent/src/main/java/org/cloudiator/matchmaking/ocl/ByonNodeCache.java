package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.cloudiator.messages.Byon.ByonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class ByonNodeCache {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(ByonNodeCache.class);
  private final ByonUpdater updater;
  private volatile Map<ByonCacheKey,ByonNode> byonNodeCache = new HashMap<>();

  @Inject
  public ByonNodeCache(ByonUpdater updater) {
    this.updater = updater;
  }

  public synchronized Optional<ByonNode> add(ByonNode node) {
    ByonCacheKey key = new ByonCacheKey(node.getId(), node.getUserId());
    if(hit(key.getNodeId(), key.getUserId()).isPresent()) {
      LOGGER.info(String.format("Overriding node with key %s in cache %s.",
          key, this));
    }

    ByonNode origByonNode = byonNodeCache.put(key, node);
    publishUpdate();

    return Optional.ofNullable(origByonNode);
  }

  public synchronized Optional<ByonNode> evict(String id, String userId) {
    ByonCacheKey key = new ByonCacheKey(id, userId);
    if (!hit(key.getNodeId(), key.getUserId()).isPresent()) {
      LOGGER.error(String.format("Cannot evict node with key %s in cache %s as key is not present", key, this));
      return Optional.empty();
    }

    ByonNode evictNode = byonNodeCache.get(key);
    publishUpdate();

    byonNodeCache.remove(key);
    return Optional.of(evictNode);
  }

  private synchronized void publishUpdate() {
    updater.update(this);
  }

  public synchronized Optional<ByonNode> hit(String id, String userId) {
    ByonCacheKey key = new ByonCacheKey(id, userId);
    return (byonNodeCache.get(key) == null) ? Optional.empty()
        : Optional.of(byonNodeCache.get(key));
  }

  public synchronized  Optional<ByonNode> read(String id, String userId) {
    ByonCacheKey key = new ByonCacheKey(id, userId);
    if (!hit(key.getNodeId(), key.getUserId()).isPresent()) {
      LOGGER.error(String.format("Cannot read node with key %s in cache %s as key is not present", key, this));
      return Optional.empty();
    }

    return Optional.of(byonNodeCache.get(key));
  }

  public synchronized Set<ByonNode> readAll() {
    return byonNodeCache.values().stream().collect(
        Collectors.toSet());
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
      return nodeId.equals(cacheKey.nodeId) &&
          userId.equals(cacheKey.userId);
    }

    @Override
    public String toString() {
      return String.format("(%s,%s)",nodeId, userId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(nodeId, userId);
    }
  }
}
