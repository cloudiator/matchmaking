package org.cloudiator.matchmaking.choco;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ObjectMappers {

  private static final Map<Class, Callable<ObjectMapper>> objectMappers;

  static {
    objectMappers = new HashMap<>();
    objectMappers.put(String.class, ObjectMapperImpl::new);
    objectMappers.put(Double.class, DoubleMapper::new);
  }

  private ObjectMappers() {

  }

  public static <T> ObjectMapper<T> getObjectMapperForType(Class<T> clazz) {
    try {
      //noinspection unchecked
      return objectMappers.get(clazz).call();
    } catch (Exception e) {
      throw new IllegalStateException(
          String.format("Error retrieving object mapper for type %s", clazz), e);
    }
  }

}
