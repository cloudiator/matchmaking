package org.cloudiator.matchmaking.ocl;

import cloudiator.CloudiatorPackage;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.RepresentAsOCL;
import org.cloudiator.matchmaking.domain.Requirement;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.utilities.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OclCsp {

  private Set<String> unparsedConstraints;
  private Set<ExpressionInOCL> constraints;
  private static final Logger LOGGER = LoggerFactory.getLogger(OclCsp.class);
  @Nullable
  private final Integer minimumNodeSize;


  private OclCsp(Iterable<String> constraints, @Nullable Integer minimumNodeSize)
      throws ParserException {

    this.unparsedConstraints = Sets.newHashSet(constraints);
    this.minimumNodeSize = minimumNodeSize;
    final Builder<ExpressionInOCL> builder = ImmutableSet.<ExpressionInOCL>builder();
    for (String c : constraints) {

      LOGGER.debug(String.format("Parsing constraint %s.", c));

      ExpressionInOCL expression = OCLUtil
          .createInvariant(CloudiatorPackage.eINSTANCE.getComponent(), c);
      builder.add(expression);

    }
    this.constraints = builder.build();
  }

  public static OclCsp ofConstraints(Iterable<String> constraints,
      @Nullable Integer minimumNodeSize)
      throws ParserException {
    return new OclCsp(constraints, minimumNodeSize);
  }


  public static OclCsp ofRequirements(Collection<Requirement> requirements,
      @Nullable Integer minimumNodeSize) throws ParserException {

    Collection<String> constraints = Lists.newArrayList();
    for (Requirement requirement : requirements) {
      constraints.addAll(RepresentAsOCL.INSTANCE.apply(requirement).getOCLConstraints());
    }
    return new OclCsp(constraints, minimumNodeSize);
  }

  public Set<String> getUnparsedConstraints() {
    return unparsedConstraints;
  }

  public Set<ExpressionInOCL> getConstraints() {
    return constraints;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("constraints", constraints)
        .add("minimumNodeSize", minimumNodeSize).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OclCsp oclCsp = (OclCsp) o;
    return unparsedConstraints.equals(oclCsp.unparsedConstraints) &&
        Objects.equals(minimumNodeSize, oclCsp.minimumNodeSize);
  }

  @Override
  public int hashCode() {
    return Objects.hash(unparsedConstraints, minimumNodeSize);
  }

  @Nullable
  public Integer getMinimumNodeSize() {
    return minimumNodeSize;
  }
}
