package org.cloudiator.matchmaking.choco;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.NoSuchElementException;

public class ObjectMapperImpl<T> implements ObjectMapper<T> {

  private BiMap<Integer, T> map = HashBiMap.create();
  private int counter = 1;

  public ObjectMapperImpl() {
    map.put(0, null);
  }

  @Override
  public T applyBack(int i) {
    T value = map.get(i);
    if (value == null) {
      throw new NoSuchElementException();
    }
    return value;
  }

  @Override
  public synchronized int applyAsInt(T value) {

    Integer integer = map.inverse().get(value);

    if (integer != null) {
      return integer;
    } else {

      map.put(counter, value);
    }
    return counter++;
  }
}
