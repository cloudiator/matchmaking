package org.cloudiator.matchmaking.ocl;

import cloudiator.CloudiatorModel;
import com.google.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import javax.inject.Named;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.CacheMap;
import net.spy.memcached.MemcachedClient;

public class MemchachedModelGenerator implements ModelGenerator {

  private final ModelGenerator delegate;
  private final int cacheTime;

  @Inject
  public MemchachedModelGenerator(@Named("Base") ModelGenerator delegate,
      @Named("cacheTime") int cacheTime) {
    this.delegate = delegate;
    this.cacheTime = cacheTime;
  }

  @Override
  public CloudiatorModel generateModel(String userId) throws ModelGenerationException {

    try {
      final List<InetSocketAddress> addresses = AddrUtil.getAddresses("localhost:32768");
      MemcachedClient memcachedClient = new MemcachedClient(addresses);

      CacheMap cacheMap = new CacheMap(memcachedClient, cacheTime, "cloudiatorModel");
      Object o = cacheMap.get(userId);

      if (o == null) {
        CloudiatorModel cloudiatorModel = delegate.generateModel(userId);
        cacheMap.put(userId, cloudiatorModel);
      }

      return (CloudiatorModel) o;

    } catch (IOException e) {
      throw new ModelGenerationException(
          "Could not communicate with memcached", e);
    }
  }
}
