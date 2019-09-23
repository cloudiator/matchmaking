package org.cloudiator.matchmaking.ocl;

import com.google.inject.Inject;
import org.cloudiator.messaging.services.PricingService;

public class PricingSupplierFactory {
    private final PricingService pricingService;

    @Inject
    public PricingSupplierFactory(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    public PricingSupplier newInstance(String userId, String cloudAPIProviderName) {
        return new PricingSupplier(userId, cloudAPIProviderName, pricingService);
    }
}
