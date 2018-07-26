package org.cloudiator.matchmaking.ocl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.util.concurrent.TimeUnit;

public class CachedNodeGenerator implements NodeGenerator {

  private final Supplier<NodeCandidates> cachedSupplier;

  CachedNodeGenerator(NodeGenerator delegate, int cacheTime) {
    checkNotNull(delegate, "delegate is null");
    checkArgument(cacheTime >= 0, "cacheTime needs to be larger than zero");

    this.cachedSupplier = Suppliers.memoizeWithExpiration(delegate, cacheTime, TimeUnit.SECONDS);
  }

  @Override
  public NodeCandidates get() {
    return cachedSupplier.get();
  }
}
