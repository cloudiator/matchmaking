package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.LocationScope;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.cloudiator.messages.entities.IaasEntities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSPSourcedPricePlanPriceFunction implements PriceFunction {

  private final PricingSupplierFactory pricingSupplierFactory;

  private Map<String, Map<Long, IaasEntities.Price>> cloudUser2PriceMap;
  private static final Logger LOGGER = LoggerFactory
      .getLogger(CSPSourcedPricePlanPriceFunction.class);

  @Inject
  public CSPSourcedPricePlanPriceFunction(PricingSupplierFactory pricingSupplierFactory) {
    this.pricingSupplierFactory = pricingSupplierFactory;
    cloudUser2PriceMap = new ConcurrentHashMap<>();
  }

  @Override
  public Optional<Double> calculatePricing(Cloud cloud, Hardware hardware, Location location,
      Image image, String userId) {
    String locationProviderId =
        location.getLocationScope() == LocationScope.ZONE ? location.getParent().getProviderId()
            : location.getProviderId();

    HashCode sha256PriceKey = Hashing.sha256()
        .hashString((new StringBuilder())
            .append(hardware.getProviderId())
            .append(locationProviderId)
            .append(image.getOperatingSystem().getArchitecture().getName())
            .append(image.getOperatingSystem().getFamily().getName())
            .append(cloud.getApi().getProviderName()), StandardCharsets.UTF_8);

    Double price = Optional.ofNullable(getPriceMap(cloud, userId))
        .map(priceMap -> priceMap.get(sha256PriceKey.asLong()))
        .map(IaasEntities.Price::getPrice)
        .orElse(null);

    if (price == null) {
      LOGGER.warn(String.format(
          "No pricing data found for: CSP: %s, Location: %s, Instance: %s, OS: %s, %s. sha256 (asLong) key: %d"
          , cloud.getApi().getProviderName()
          , locationProviderId
          , hardware.getProviderId()
          , image.getOperatingSystem().getArchitecture().getName()
          , image.getOperatingSystem().getFamily().getName()
          , sha256PriceKey.asLong()));
    }

    return Optional.ofNullable(price);
  }

  private Map<Long, IaasEntities.Price> getPriceMap(Cloud cloud, String userId) {
    String cloudUserKey = cloud.getId() + userId;

    return cloudUser2PriceMap.computeIfAbsent(cloudUserKey,
        k -> pricingSupplierFactory.newInstance(userId, cloud.getApi().getProviderName()).get());
  }

  @Override
  public int getPriority() {
    return Priority.HIGH;
  }
}
