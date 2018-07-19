package org.cloudiator.matchmaking.domain;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Solution implements Comparable<Solution> {

  public static final Solution EMPTY_SOLUTION = Solution.of(Collections.emptyList());

  private final List<NodeCandidate> nodeCandidates;
  private Double costs = null;
  private Float time = null;
  private Boolean isOptimal = false;

  private Solution(Collection<NodeCandidate> candidates) {
    this.nodeCandidates = new ArrayList<>(candidates);
  }

  public static Solution of(Collection<NodeCandidate> candidates) {
    return new Solution(candidates);
  }

  public List<NodeCandidate> getList() {
    return nodeCandidates;
  }

  public int nodeSize() {
    return getList().size();
  }

  public void setIsOptimal(boolean isOptimal) {
    this.isOptimal = isOptimal;
  }

  public boolean isOptimal() {
    return isOptimal;
  }

  public boolean noSolution() {
    return nodeCandidates.isEmpty();
  }

  public Optional<Float> getTime() {
    return Optional.ofNullable(time);
  }

  public void setTime(float time) {
    this.time = time;
  }

  public Double getCosts() {
    if (costs == null) {
      costs = nodeCandidates.stream().mapToDouble(NodeCandidate::getPrice).sum();
    }
    return costs;
  }

  @Override
  public int compareTo(Solution o) {
    return this.getCosts().compareTo(o.getCosts());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("time", getTime()).add("price", getCosts())
        .add("numberOfNodes", nodeSize())
        .add("nodes", nodeCandidates)
        .toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((nodeCandidates == null) ? 0 : nodeCandidates.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Solution other = (Solution) obj;
    if (nodeCandidates == null) {
      if (other.nodeCandidates != null) {
        return false;
      }
    } else if (!nodeCandidates.equals(other.nodeCandidates)) {
      return false;
    }
    return true;
  }


}
