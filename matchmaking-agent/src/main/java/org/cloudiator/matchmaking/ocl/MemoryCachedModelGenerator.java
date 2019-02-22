package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkArgument;

import cloudiator.CloudiatorModel;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.cloudiator.matchmaking.Expirable;

@Singleton
public class MemoryCachedModelGenerator implements ModelGenerator, Expirable {

  private final Cache<String, CloudiatorModel> modelCache;
  private final ModelGenerator delegate;

  @Inject
  public MemoryCachedModelGenerator(@Named("Base") ModelGenerator delegate,
      @Named("cacheTime") int cacheTime) {

    checkArgument(cacheTime >= 0, "cacheTime needs to be larger than zero");

    this.modelCache = CacheBuilder.newBuilder()
        .expireAfterWrite(cacheTime,
            TimeUnit.SECONDS)
        .build();

    this.delegate = delegate;
  }


  @Override
  public CloudiatorModel generateModel(String userId) throws ModelGenerationException {

    try {
      return modelCache.get(userId, () -> delegate.generateModel(userId));
    } catch (ExecutionException e) {
      if (e.getCause() instanceof ModelGenerationException) {
        throw (ModelGenerationException) e.getCause();
      }
      throw new IllegalStateException("Unexpected exception during generation of model.",
          e.getCause());
    }
  }

  @Override
  public void expire(String userId) {
    modelCache.invalidate(userId);
  }
}
