package org.cloudiator.matchmaking.ocl;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.cloudiator.messages.Pricing.PricingQueryRequest;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.PricingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PricingSupplier implements Supplier<Map<Long, IaasEntities.Price>> {

  private final String userId;
  private final String cloudAPIProviderName;
  private final PricingService pricingService;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(PricingSupplier.class);

  public PricingSupplier(String userId, String cloudAPIProviderName,
      PricingService pricingService) {
    this.userId = userId;
    this.cloudAPIProviderName = cloudAPIProviderName;
    this.pricingService = pricingService;
  }

  @Override
  public Map<Long, IaasEntities.Price> get() {
    PricingQueryRequest pricingQueryRequest = PricingQueryRequest.newBuilder()
        .setCloudAPIProviderName(cloudAPIProviderName)
        .setUserId(userId)
        .build();

    try {
      List<IaasEntities.Price> prices = pricingService.getPrice(pricingQueryRequest)
          .getPricesList();

      return prices.stream().collect(Collectors.toMap(price -> {
        HashCode sha256PriceKey = Hashing.sha256()
            .hashString((new StringBuilder())
                .append(price.getHardwareProviderId())
                .append(price.getLocationProviderId())
                .append(price.getOsArchitecture())
                .append(price.getOsFamily())
                .append(price.getCloudAPIProviderName()), StandardCharsets.UTF_8);
        return sha256PriceKey.asLong();
      }, price -> price));
    } catch (ResponseException e) {

      LOGGER.warn(
          String.format("Could not retrieve price due to communication error: %s", e.getMessage()),
          e);
      return Collections.emptyMap();
    }
  }
}
