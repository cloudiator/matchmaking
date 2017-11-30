package org.cloudiator.choco;

import java.util.Collection;
import java.util.Comparator;

public class SortedObjectMapper<T> implements ObjectMapper<T> {

  private final ObjectMapper<T> delegate = new ObjectMapperImpl<>();

  public SortedObjectMapper(Collection<? extends T> collection, Comparator<? super T> comparator) {
    collection.stream().sorted(comparator).forEach(delegate::applyAsInt);
  }

  @Override
  public T applyBack(int i) {
    return delegate.applyBack(i);
  }

  @Override
  public int applyAsInt(T t) {
    return delegate.applyAsInt(t);
  }
}
