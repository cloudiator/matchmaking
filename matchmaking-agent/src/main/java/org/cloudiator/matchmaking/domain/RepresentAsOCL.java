package org.cloudiator.matchmaking.domain;

import java.util.Collections;
import java.util.function.Function;

public class RepresentAsOCL implements Function<Requirement, RepresentableAsOCL> {

  public static final RepresentAsOCL INSTANCE = new RepresentAsOCL();

  private RepresentAsOCL() {

  }

  @Override
  public RepresentableAsOCL apply(Requirement requirement) {

    if (requirement instanceof AttributeRequirement) {
      return new AttributeRequirementInOCL((AttributeRequirement) requirement);
    } else if (requirement instanceof IdRequirement) {
      return new IdRequirementInOCL((IdRequirement) requirement);
    } else if (requirement instanceof OclRequirement) {
      return () -> Collections.singleton(((OclRequirement) requirement).constraint());
    } else {
      throw new AssertionError("Unknown requirement type " + requirement.getClass().getName());
    }
  }
}
