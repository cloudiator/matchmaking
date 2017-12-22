package org.cloudiator.ocl;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Solution implements Comparable<Solution> {

  public static final Solution EMPTY_SOLUTION = Solution.of(Collections.emptyList());

  private final List<NodeCandidate> nodeCandiates;
  private Double costs = null;
  private Float time = null;
  private Boolean isOptimal = null;

  private Solution(Collection<NodeCandidate> candidates) {
    this.nodeCandiates = new ArrayList<>(candidates);
  }

  public static Solution of(Collection<NodeCandidate> candidates) {
    return new Solution(candidates);
  }

  public List<NodeCandidate> getList() {
    return nodeCandiates;
  }

  public int nodeSize() {
    return getList().size();
  }

  public void setIsOptimal(boolean isOptimal) {
    this.isOptimal = isOptimal;
  }

  public boolean isOptimal() {
    if (isOptimal == null) {
      throw new IllegalStateException("optimal not set");
    }
    return isOptimal;
  }

  public boolean noSolution() {
    return nodeCandiates.isEmpty();
  }

  public float getTime() {
    if (time == null) {
      throw new IllegalStateException("time not set");
    }
    return time;
  }

  public void setTime(float time) {
    this.time = time;
  }

  public Double getCosts() {
    if (costs == null) {
      costs = nodeCandiates.stream().mapToDouble(NodeCandidate::getPrice).sum();
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
        .add("nodes", nodeCandiates)
        .toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((nodeCandiates == null) ? 0 : nodeCandiates.hashCode());
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
    if (nodeCandiates == null) {
      if (other.nodeCandiates != null) {
        return false;
      }
    } else if (!nodeCandiates.equals(other.nodeCandiates)) {
      return false;
    }
    return true;
  }


}
