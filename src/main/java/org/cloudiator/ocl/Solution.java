package org.cloudiator.ocl;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Solution implements Comparable<Solution> {

  private List<NodeCandidate> nodeCandiates;
  private int fitness = -1;
  private Double costs = null;
  private long time = -1;

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

  public void setTime(long time) {
    this.time = time;
  }

  public long getTime() {
    if (time == -1) {
      throw new IllegalStateException("time not set");
    }
    return time;
  }

  public Double getCosts() {
    if (costs == null) {
      costs = nodeCandiates.stream().mapToDouble(NodeCandidate::getPrice).sum();
    }
    return costs;
  }

  public int getFitness() {
    if (fitness == -1) {
      throw new IllegalStateException("fitness not set");
    }
    return fitness;
  }

  public void setFitness(int fitness) {
    if (this.fitness != -1) {
      throw new IllegalStateException("fitness already set");
    }
    this.fitness = fitness;
  }

  @Override
  public int compareTo(Solution o) {
    return this.getCosts().compareTo(o.getCosts());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("price", getCosts()).add("nodes", nodeCandiates)
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
