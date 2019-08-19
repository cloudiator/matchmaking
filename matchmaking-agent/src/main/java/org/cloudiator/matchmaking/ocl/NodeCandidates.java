package org.cloudiator.matchmaking.ocl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.cloudiator.matchmaking.domain.NodeCandidate;

public class NodeCandidates implements Set<NodeCandidate>, NodeGenerator {

  private final Map<String, NodeCandidate> nodeCandidates = new HashMap<>();

  private NodeCandidates(Set<NodeCandidate> nodeCandidates) {
    for (NodeCandidate nodeCandidate : nodeCandidates) {
      this.nodeCandidates.put(nodeCandidate.id(), nodeCandidate);
    }
  }

  public static NodeCandidates of(Set<NodeCandidate> nodeCandidates) {
    return new NodeCandidates(nodeCandidates);
  }

  public static NodeCandidates single(NodeCandidate nodeCandidate) {
    return new NodeCandidates(Collections.singleton(nodeCandidate));
  }

  public static NodeCandidates empty() {
    return new NodeCandidates(Collections.emptySet());
  }

  @Override
  public int size() {
    return nodeCandidates.size();
  }

  @Override
  public boolean isEmpty() {
    return nodeCandidates.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return nodeCandidates.containsValue(o);
  }

  @Override
  public Iterator<NodeCandidate> iterator() {
    return nodeCandidates.values().iterator();
  }

  @Override
  public Object[] toArray() {
    return nodeCandidates.values().toArray();
  }

  @Override
  public <T> T[] toArray(T[] ts) {
    return nodeCandidates.values().toArray(ts);
  }

  @Override
  public boolean add(NodeCandidate nodeCandidate) {
    return nodeCandidates.put(nodeCandidate.id(), nodeCandidate) == null;
  }

  @Override
  public boolean remove(Object o) {
    return nodeCandidates.values().remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    return nodeCandidates.values().containsAll(collection);
  }

  @Override
  public boolean addAll(Collection<? extends NodeCandidate> collection) {
    for (NodeCandidate nodeCandidate : collection) {
      nodeCandidates.put(nodeCandidate.id(), nodeCandidate);
    }
    return true;
  }

  @Override
  public boolean retainAll(Collection<?> collection) {
    return nodeCandidates.values().retainAll(collection);
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    return nodeCandidates.values().removeAll(collection);
  }

  @Override
  public void clear() {
    nodeCandidates.clear();
  }

  @Override
  public boolean equals(Object o) {
    return nodeCandidates.equals(o);
  }

  @Override
  public int hashCode() {
    return nodeCandidates.hashCode();
  }

  @Override
  public Spliterator<NodeCandidate> spliterator() {
    return nodeCandidates.values().spliterator();
  }

  @Override
  public boolean removeIf(Predicate<? super NodeCandidate> predicate) {

    return nodeCandidates.values().remove(predicate);
  }

  @Override
  public Stream<NodeCandidate> stream() {
    return nodeCandidates.values().stream();
  }

  @Override
  public Stream<NodeCandidate> parallelStream() {
    return nodeCandidates.values().parallelStream();
  }

  @Override
  public void forEach(Consumer<? super NodeCandidate> consumer) {
    nodeCandidates.values().forEach(consumer);
  }

  @Override
  public NodeCandidates get() {
    return this;
  }

  public NodeCandidate getById(String string) {
    return nodeCandidates.get(string);
  }
}
