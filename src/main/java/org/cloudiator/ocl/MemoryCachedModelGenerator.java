package org.cloudiator.ocl;

import cloudiator.CloudiatorModel;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

public class MemoryCachedModelGenerator implements ModelGenerator {

  private final ModelGenerator delegate;

  private final LoadingCache<String, CloudiatorModel> modelCache = CacheBuilder.newBuilder()
      .build(
          new CacheLoader<String, CloudiatorModel>() {
            public CloudiatorModel load(@Nullable String userId)
                throws ModelGenerationException { // no checked exception
              return delegate.generateModel(userId);
            }
          });

  @Inject
  public MemoryCachedModelGenerator(@Named("Base") ModelGenerator delegate) {
    this.delegate = delegate;
  }


  @Override
  public CloudiatorModel generateModel(String userId) throws ModelGenerationException {
    try {
      return modelCache.get(userId);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof ModelGenerationException) {
        throw (ModelGenerationException) e.getCause();
      }
      throw new IllegalStateException(e.getCause());
    }
  }
}
