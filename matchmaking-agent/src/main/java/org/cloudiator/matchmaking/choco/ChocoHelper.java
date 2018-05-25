package org.cloudiator.matchmaking.choco;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;

public class ChocoHelper {

  private ChocoHelper() {
    throw new AssertionError("Do not instantiate");
  }

  public static Set<Integer> getDomainOfVariable(IntVar intVar) {
    Set<Integer> domain = new HashSet<>();
    final DisposableValueIterator valueIterator = intVar.getValueIterator(true);
    while (valueIterator.hasNext()) {
      domain.add(valueIterator.next());
    }
    valueIterator.dispose();
    return domain;
  }

  public static Set<Integer> getMergedDomainOfVariables(Collection<IntVar> intVars) {
    Set<Integer> integerSet = new HashSet<>();
    intVars.forEach(integers -> integerSet.addAll(getDomainOfVariable(integers)));
    return integerSet;
  }
}
