package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import org.cloudiator.matchmaking.domain.Solution;

@SuppressWarnings("UnstableApiUsage")
@Singleton
public class SolutionCacheImpl implements SolutionCache {

  private static final int LIVE_DURATION = 10;
  private static final int EXPIRED_DURATION = 30;

  private static final Cache<CacheKey, Solution> EXPIRED_CACHE = CacheBuilder.newBuilder()
      .expireAfterWrite(EXPIRED_DURATION, TimeUnit.MINUTES).build();

  private static final RemovalListener<CacheKey, Solution> REMOVAL_LISTENER = new RemovalListener<CacheKey, Solution>() {
    public void onRemoval(RemovalNotification<CacheKey, Solution> removal) {
      if (removal.getKey() == null || removal.getValue() == null) {
        return;
      }
      Solution expired = removal.getValue();
      expired.expire();
      EXPIRED_CACHE.put(removal.getKey(), expired);
    }
  };

  private static final Cache<CacheKey, Solution> LIVE_CACHE = CacheBuilder.newBuilder()
      .expireAfterWrite(LIVE_DURATION, TimeUnit.MINUTES).removalListener(
          REMOVAL_LISTENER).build();


  @Override
  public void storeSolution(String userId, OclCsp oclCsp, Solution solution) {
    checkState(solution.isValid(), "Storing invalid solutions is not permitted.");
    LIVE_CACHE.put(new CacheKey(userId, oclCsp), solution);
  }

  @Override
  public Optional<Solution> retrieve(String userId, String id) {

    final Optional<Solution> liveSolution = searchInCache(userId, id, LIVE_CACHE);
    if (liveSolution.isPresent()) {
      return liveSolution;
    } else {
      return searchInCache(userId, id, EXPIRED_CACHE);
    }
  }

  private Optional<Solution> searchInCache(String userId, String id,
      Cache<CacheKey, Solution> cache) {
    for (Entry<CacheKey, Solution> entry : LIVE_CACHE.asMap().entrySet()) {
      if (entry.getKey().getUserId().equals(userId) && entry.getValue().getId().equals(id)) {
        return Optional.of(entry.getValue());
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<Solution> retrieve(String userId, OclCsp oclCsp) {

    final CacheKey cacheKey = new CacheKey(userId, oclCsp);

    final Solution ifPresent = LIVE_CACHE.getIfPresent(cacheKey);

    if (ifPresent != null) {
      return Optional.of(ifPresent);
    }

    return Optional.ofNullable(EXPIRED_CACHE.getIfPresent(cacheKey));
  }


  @Override
  public void expire(String userId) {
    LIVE_CACHE.asMap().forEach((cacheKey, solution) -> {
      if (cacheKey.getUserId().equals(userId)) {
        LIVE_CACHE.invalidate(solution);
      }
    });
  }


  private static class CacheKey {

    private final String userId;
    private final OclCsp oclCsp;

    private CacheKey(String userId, OclCsp oclCsp) {
      checkNotNull(userId, "userId is null");
      checkNotNull(oclCsp, "oclCsp is null");
      this.userId = userId;
      this.oclCsp = oclCsp;
    }


    public String getUserId() {
      return userId;
    }

    public OclCsp getOclCsp() {
      return oclCsp;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CacheKey cacheKey = (CacheKey) o;
      return userId.equals(cacheKey.userId) &&
          oclCsp.equals(cacheKey.oclCsp);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userId, oclCsp);
    }
  }
}
