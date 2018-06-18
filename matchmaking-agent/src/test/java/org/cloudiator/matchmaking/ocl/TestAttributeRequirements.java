package org.cloudiator.matchmaking.ocl;

import java.util.HashSet;
import java.util.Set;
import org.cloudiator.matchmaking.domain.AttributeRequirement;
import org.cloudiator.matchmaking.domain.AttributeRequirementBuilder;
import org.cloudiator.matchmaking.domain.AttributeRequirementInOCL;
import org.cloudiator.matchmaking.domain.Requirement;
import org.cloudiator.matchmaking.domain.RequirementOperator;

public class TestAttributeRequirements {

  private TestAttributeRequirements() {
    throw new AssertionError("Do not instantiate");
  }

  public static final Set<Requirement> TEST_REQUIREMENTS = new HashSet<Requirement>() {{
    add(inRequirement());
  }};

  private static AttributeRequirement inRequirement() {
    //add("nodes->forAll(n | Set{'test','test2','test3'}->includes(n.location.geoLocation.country))");
    return AttributeRequirementBuilder.newBuilder()
        .requirementAttribute("country")
        .requirementClass("location.geoLocation").requirementOperator(RequirementOperator.IN)
        .value("ulm, stuttgart, freiburg").build();
  }

}
