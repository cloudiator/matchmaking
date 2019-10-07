package org.cloudiator.matchmaking.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

public class Solution implements Comparable<Solution> {

  public static final Solution EMPTY_SOLUTION = Solution.of(Collections.emptyList());

  private final String id;
  private final List<NodeCandidate> nodeCandidates;
  private Double costs = null;
  private Float time = null;
  private boolean isOptimal = false;
  private boolean valid = true;
  private Class<? extends Solver> solver;

  private Solution(Collection<NodeCandidate> candidates, @Nullable String id) {
    this.nodeCandidates = new ArrayList<>(candidates);
    if (id != null) {
      this.id = id;
    } else {
      this.id = UUID.randomUUID().toString();
    }
  }

  public static Solution of(Collection<NodeCandidate> candidates) {
    return new Solution(candidates, null);
  }

  public static Solution of(String id, Collection<NodeCandidate> candidates) {
    checkNotNull(id, "id is null");
    return new Solution(candidates, id);
  }

  public void setSolver(Class<? extends Solver> solver) {
    this.solver = solver;
  }

  public Class<? extends Solver> getSolver() {
    return solver;
  }

  public void expired() {
    this.valid = false;
  }

  public List<NodeCandidate> getNodeCandidates() {
    return nodeCandidates;
  }

  public int nodeSize() {
    return getNodeCandidates().size();
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
    return MoreObjects.toStringHelper(this).add("valid", valid).add("optimal", isOptimal())
        .add("solver", solver.getCanonicalName())
        .add("time", getTime())
        .add("price", getCosts())
        .add("numberOfNodes", nodeSize())
        .add("nodes", nodeCandidates)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Solution solution = (Solution) o;
    return Objects.equals(nodeCandidates, solution.nodeCandidates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nodeCandidates);
  }

  public boolean isValid() {
    return valid;
  }

  public String getId() {
    return id;
  }
}
