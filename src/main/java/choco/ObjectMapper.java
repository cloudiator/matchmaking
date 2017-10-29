package choco;

import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

public interface ObjectMapper<T> extends ToIntFunction<T> {

  T applyBack(int i);

  default int[] applyMultipleToInt(Iterable<? extends T> iterable) {
    Set<Integer> integers = new HashSet<>();
    for (T t : iterable) {
      integers.add(applyAsInt(t));
    }
    return integers.stream().mapToInt(Number::intValue).toArray();
  }

}
