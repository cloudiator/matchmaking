package org.cloudiator.matchmaking.ocl;

import cloudiator.CloudiatorModel;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.CacheMap;
import net.spy.memcached.MemcachedClient;

import javax.inject.Named;
import java.io.IOException;

public class MemchachedModelGenerator implements ModelGenerator {

    private final ModelGenerator delegate;
    private final int cacheTime;

    public MemchachedModelGenerator(@Named("Base") ModelGenerator delegate,
                                    @Named("cacheTime") int cacheTime) {
        this.delegate = delegate;
        this.cacheTime = cacheTime;
    }

    @Override
    public CloudiatorModel generateModel(String userId) throws ModelGenerationException {

        try {
            MemcachedClient memcachedClient = new MemcachedClient(AddrUtil.getAddresses("localhost:11211"));

            CacheMap cacheMap = new CacheMap(memcachedClient, cacheTime, "cloudiatorModel");
            Object o = cacheMap.get(userId);

            if (o == null) {
                CloudiatorModel cloudiatorModel = delegate.generateModel(userId);
                cacheMap.put(userId, cloudiatorModel);
            }

            return (CloudiatorModel) o;


        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
