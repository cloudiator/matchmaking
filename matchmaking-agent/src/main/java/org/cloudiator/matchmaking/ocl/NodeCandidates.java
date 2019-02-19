package org.cloudiator.matchmaking.ocl;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.cloudiator.matchmaking.domain.NodeCandidate;

public class NodeCandidates implements Set<NodeCandidate>, NodeGenerator {

  private final Set<NodeCandidate> nodeCandidates;

  private NodeCandidates(Set<NodeCandidate> nodeCandidates) {
    this.nodeCandidates = ImmutableSet.copyOf(nodeCandidates);
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
    return nodeCandidates.contains(o);
  }

  @Override
  public Iterator<NodeCandidate> iterator() {
    return nodeCandidates.iterator();
  }

  @Override
  public Object[] toArray() {
    return nodeCandidates.toArray();
  }

  @Override
  public <T> T[] toArray(T[] ts) {
    return nodeCandidates.toArray(ts);
  }

  @Override
  public boolean add(NodeCandidate nodeCandidate) {
    return nodeCandidates.add(nodeCandidate);
  }

  @Override
  public boolean remove(Object o) {
    return nodeCandidates.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    return nodeCandidates.containsAll(collection);
  }

  @Override
  public boolean addAll(Collection<? extends NodeCandidate> collection) {
    return nodeCandidates.addAll(collection);
  }

  @Override
  public boolean retainAll(Collection<?> collection) {
    return nodeCandidates.retainAll(collection);
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    return nodeCandidates.removeAll(collection);
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
    return nodeCandidates.spliterator();
  }

  @Override
  public boolean removeIf(Predicate<? super NodeCandidate> predicate) {
    return nodeCandidates.removeIf(predicate);
  }

  @Override
  public Stream<NodeCandidate> stream() {
    return nodeCandidates.stream();
  }

  @Override
  public Stream<NodeCandidate> parallelStream() {
    return nodeCandidates.parallelStream();
  }

  @Override
  public void forEach(Consumer<? super NodeCandidate> consumer) {
    nodeCandidates.forEach(consumer);
  }

  @Override
  public NodeCandidates get() {
    return this;
  }
}
