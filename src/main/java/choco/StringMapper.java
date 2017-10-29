package choco;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.NoSuchElementException;

public class StringMapper implements ObjectMapper<String> {

  private BiMap<Integer, String> map = HashBiMap.create();
  private int counter = 1;

  @Override
  public String applyBack(int i) {
    String value = map.get(i);
    if (value == null) {
      throw new NoSuchElementException();
    }
    return value;
  }

  @Override
  public synchronized int applyAsInt(String value) {

    Integer integer = map.inverse().get(value);

    if (integer != null) {
      return integer;
    } else {

      map.put(counter, value);
    }
    return counter++;
  }
}
