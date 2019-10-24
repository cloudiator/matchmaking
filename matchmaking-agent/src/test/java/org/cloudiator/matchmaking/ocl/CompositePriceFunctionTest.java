package org.cloudiator.matchmaking.ocl;

import de.uniulm.omi.cloudiator.util.execution.Prioritized;
import java.util.Iterator;
import java.util.TreeSet;
import org.junit.Test;

public class CompositePriceFunctionTest {

  @Test
  public void testPriority() {

    final CSPSourcedPricePlanPriceFunction cspSourcedPricePlanPriceFunction = new CSPSourcedPricePlanPriceFunction(
        null);
    final HardwareBasedPriceFunction hardwareBasedPriceFunction = new HardwareBasedPriceFunction();

    final TreeSet<PriceFunction> priceFunctions = new TreeSet<>(Prioritized::compareTo);
    priceFunctions.add(cspSourcedPricePlanPriceFunction);
    priceFunctions.add(hardwareBasedPriceFunction);

    final Iterator<PriceFunction> iterator = priceFunctions.iterator();
    final PriceFunction first = iterator.next();
    final PriceFunction second = iterator.next();

    assert first instanceof CSPSourcedPricePlanPriceFunction;
    assert second instanceof HardwareBasedPriceFunction;


  }

}
