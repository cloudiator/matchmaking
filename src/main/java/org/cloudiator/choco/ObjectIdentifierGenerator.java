package org.cloudiator.choco;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.ecore.EClass;

public class ObjectIdentifierGenerator {

  private final Map<EClass, OidHandler> oidHandlerStore = new HashMap<>();

  private ObjectIdentifierGenerator() {

  }

  public static ObjectIdentifierGenerator create() {
    return new ObjectIdentifierGenerator();
  }

  public synchronized int generateIdFor(EClass eclass, Object o) {
    if (!oidHandlerStore.containsKey(eclass)) {
      oidHandlerStore.put(eclass, new OidHandler());
    }
    return oidHandlerStore.get(eclass).get(o);
  }

  private static class OidHandler {

    private Integer counter = 0;
    private final Map<Object, Integer> objectIdMap = new HashMap<>();


    private OidHandler() {
    }

    private int next() {
      return ++counter;
    }

    synchronized int get(Object o) {
      if (!objectIdMap.containsKey(o)) {
        objectIdMap.put(o, next());
      }
      return objectIdMap.get(o);
    }

  }

}
