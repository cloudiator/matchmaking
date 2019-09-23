package org.cloudiator.matchmaking.ocl;

import cloudiator.*;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import org.cloudiator.messages.entities.IaasEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CSPSourcedPricePlanPriceFunction implements PriceFunction {
    private final PricingSupplierFactory pricingSupplierFactory;
    private Map<Long, IaasEntities.Price> priceMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(CSPSourcedPricePlanPriceFunction.class);

    @Inject
    public CSPSourcedPricePlanPriceFunction(PricingSupplierFactory pricingSupplierFactory) {
        this.pricingSupplierFactory = pricingSupplierFactory;
    }

    @Override
    public double calculatePricing(Cloud cloud, Hardware hardware, Location location, Image image, String userId) {
        String locationProviderId = location.getLocationScope() == LocationScope.ZONE ? location.getParent().getProviderId() : location.getProviderId();

        HashCode sha256PriceKey = Hashing.sha256()
                .hashString((new StringBuilder())
                        .append(hardware.getProviderId())
                        .append(locationProviderId)
                        .append(image.getOperatingSystem().getArchitecture().getName())
                        .append(image.getOperatingSystem().getFamily().getName())
                        .append(cloud.getApi().getProviderName()), StandardCharsets.UTF_8);

        if(!getPriceMap(cloud, userId).containsKey(sha256PriceKey.asLong())) {
            LOGGER.warn(String.format("No pricing data found for: CSP: %s, Location: %s, Instance: %s, OS: %s, %s. sha256 (asLong) key: %d"
                    ,cloud.getApi().getProviderName()
                    ,locationProviderId
                    ,hardware.getProviderId()
                    ,image.getOperatingSystem().getArchitecture().getName()
                    ,image.getOperatingSystem().getFamily().getName()
                    ,sha256PriceKey.asLong()));
            return -1;
        }

        return getPriceMap(cloud, userId).get(sha256PriceKey.asLong()).getPrice();
    }

    private Map<Long, IaasEntities.Price> getPriceMap(Cloud cloud, String userId) {
        if (priceMap == null) {
            priceMap = pricingSupplierFactory.newInstance(userId, cloud.getApi().getProviderName()).get();
        }
        return priceMap;
    }
}
