package org.cloudiator.matchmaking.choco;

import java.math.BigDecimal;

public class DoubleMapper implements ObjectMapper<Double> {

  public DoubleMapper() {
  }

  @Override
  public Double applyBack(int i) {
    return ((double) i) / 100;
  }

  @Override
  public int applyAsInt(Double value) {

    if ((value % 1) == 0) {
      return (int) value.doubleValue();
    }

    BigDecimal bigDecimal = BigDecimal.valueOf(value).setScale(100, BigDecimal.ROUND_HALF_DOWN);
    return bigDecimal.multiply(BigDecimal.valueOf(100)).intValue();
  }
}
