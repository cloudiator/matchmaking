package org.cloudiator.matchmaking.ocl;

import de.uniulm.omi.cloudiator.sword.domain.AttributeQuota;
import de.uniulm.omi.cloudiator.sword.domain.OfferQuota;
import de.uniulm.omi.cloudiator.sword.domain.Quota;
import de.uniulm.omi.cloudiator.sword.domain.QuotaSet;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.LocationUtil;
import org.cloudiator.matchmaking.domain.Solution;

public class QuotaAwareSolutionGenerator implements SolutionGenerator {

  private final SolutionGenerator delegate;
  private final QuotaSet quotaSet;

  public QuotaAwareSolutionGenerator(QuotaSet quotaSet, SolutionGenerator delegate) {
    this.delegate = delegate;
    this.quotaSet = quotaSet;
  }

  @Override
  public int nodeCandidatesSize() {
    return 0;
  }

  private boolean checkSolution(Solution solution) {
    for (Quota quota : quotaSet.quotaSet()) {
      if (!checkSolutionForQuota(solution, quota)) {
        return false;
      }
    }
    return true;
  }

  private static boolean checkSolutionForQuota(Solution solution, Quota quota) {

    if (!quota.locationId().isPresent()) {
      return true;
    }

    if (quota instanceof AttributeQuota) {
      return checkSolutionForAttributeQuota(solution, (AttributeQuota) quota);
    } else if (quota instanceof OfferQuota) {
      return checkSolutionForOfferQuota(solution, (OfferQuota) quota);
    } else {
      throw new AssertionError("Unknown quota type " + quota.getClass().getName());
    }
  }

  private static boolean checkSolutionForOfferQuota(Solution solution, OfferQuota offerQuota) {

    long count;
    switch (offerQuota.type()) {
      case HARDWARE:
        count = solution.getNodeCandidates().stream()
            .filter(nc -> LocationUtil.inHierarchy(offerQuota.locationId().get(), nc.getLocation()))
            .filter(nc -> nc.getHardware().getId().equals(offerQuota.id())).count();
        break;
      default:
        throw new AssertionError("Unsupported offer quota type " + offerQuota.type());
    }

    return offerQuota.remaining().compareTo(BigDecimal.valueOf(count)) >= 0;
  }

  private static boolean checkSolutionForAttributeQuota(Solution solution,
      AttributeQuota attributeQuota) {

    int used;
    switch (attributeQuota.attribute()) {
      case HARDWARE_CORES:
        used = solution.getNodeCandidates().stream().filter(
            nc -> LocationUtil.inHierarchy(attributeQuota.locationId().get(), nc.getLocation()))
            .mapToInt(nc -> nc.getHardware().getCores())
            .sum();
        break;
      case HARDWARE_RAM:
        used = solution.getNodeCandidates().stream().filter(
            nc -> LocationUtil.inHierarchy(attributeQuota.locationId().get(), nc.getLocation()))
            .mapToInt(nc -> nc.getHardware().getRam())
            .sum();
        break;
      case NODES_SIZE:
        used = (int) solution.getNodeCandidates().stream().filter(
            nc -> LocationUtil.inHierarchy(attributeQuota.locationId().get(), nc.getLocation()))
            .count();
        break;
      default:
        throw new AssertionError("Unknown attribute " + attributeQuota.attribute());
    }

    return attributeQuota.remaining().intValue() >= used;

  }

  @Override
  public List<Solution> generateInitialSolutions() {
    return delegate.generateInitialSolutions().stream().filter(this::checkSolution).collect(
        Collectors.toList());
  }

  @Override
  public List<Solution> getChilds(Solution solution) {
    return delegate.getChilds(solution).stream().filter(this::checkSolution).collect(
        Collectors.toList());
  }
}
