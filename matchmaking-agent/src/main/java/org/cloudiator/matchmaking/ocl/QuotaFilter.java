package org.cloudiator.matchmaking.ocl;

import cloudiator.CloudiatorModel;
import cloudiator.Location;
import de.uniulm.omi.cloudiator.sword.domain.OfferQuota;
import de.uniulm.omi.cloudiator.sword.domain.Quota;
import de.uniulm.omi.cloudiator.sword.domain.QuotaSet;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.cloudiator.matchmaking.LocationUtil;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuotaFilter implements NodeGenerator {

  private final CloudiatorModel cloudiatorModel;
  private final NodeGenerator delegate;
  private final QuotaSet quotaSet;
  private static final Logger LOGGER = LoggerFactory.getLogger(QuotaFilter.class);

  public QuotaFilter(CloudiatorModel cloudiatorModel, NodeGenerator delegate,
      QuotaSet quotaSet) {
    this.cloudiatorModel = cloudiatorModel;
    this.delegate = delegate;
    this.quotaSet = quotaSet;
  }

  private boolean checkQuota(NodeCandidate nodeCandidate) {
    for (Quota quota : quotaSet.quotaSet()) {
      if (quota.remaining().equals(BigDecimal.ZERO) && quota instanceof OfferQuota) {
        if (quota.locationId().isPresent()) {
          final Optional<Location> location = LocationUtil
              .findLocation(quota.locationId().get(), cloudiatorModel);
          if (location.isPresent()) {
            final Set<Location> locations = LocationUtil
                .subLocations(location.get(), cloudiatorModel);
            locations.add(location.get());
            for (Location affected : locations) {
              if (nodeCandidate.getLocation().getId().equals(affected.getId())) {
                switch (((OfferQuota) quota).type()) {
                  case HARDWARE:
                    if (nodeCandidate.getHardware().getId().equals(((OfferQuota) quota).id())) {
                      return false;
                    }
                  default:
                    return true;
                }
              }
            }
          }
        }
      }
    }
    return true;
  }

  @Override
  public NodeCandidates get() {

    final NodeCandidates nodeCandidates = delegate.get();
    Set<NodeCandidate> passed = new HashSet<>(nodeCandidates.size());
    for (NodeCandidate nodeCandidate : nodeCandidates) {
      if (checkQuota(nodeCandidate)) {
        passed.add(nodeCandidate);
      } else {
        LOGGER
            .debug(String.format("Filtered %s as no quota exists for this offer.", nodeCandidate));
      }
    }

    return NodeCandidates.of(passed);
  }
}
