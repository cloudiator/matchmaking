package org.cloudiator.ocl;

import cloudiator.CloudiatorPackage;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import org.cloudiator.domain.RepresentableAsOCL;
import org.eclipse.ocl.pivot.ExpressionInOCL;
import org.eclipse.ocl.pivot.utilities.ParserException;

public class OclCsp {

  //Set<String> constraints = new HashSet<>();
  //constraints.add("nodes->exists(location.name = 'RegionOne')");
  //constraints.add("nodes->forAll(n | n.hardware.cores >= 2)");
  //constraints.add("nodes->isUnique(n | n.location.name)");
  //constraints.add("nodes->forAll(n | n.hardware.ram >= 1024)");
  //constraints.add("nodes->forAll(n | n.hardware.cores >= 4 implies n.hardware.ram >= 4096)");
  //constraints.add("nodes->forAll(n | n.image.operatingSystem.family = OSFamily::UBUNTU)");
  //constraints.add("nodes->forAll(n | n.image.operatingSystem.version = '1')");
  //constraints
  //    .add("nodes->forAll(n | n.image.operatingSystem.architecture = OSArchitecture::AMD64)");
  //constraints.add("nodes->select(n | n.hardware.cores > 4)->size() >= 2");
  //constraints.add("nodes.hardware.cores->sum() >= 15");

  private Set<String> unparsedConstraints;
  private Set<ExpressionInOCL> constraints;

  private OclCsp(Collection<String> constraints) throws ParserException {
    this.unparsedConstraints = Sets.newHashSet(constraints);
    final Builder<ExpressionInOCL> builder = ImmutableSet.<ExpressionInOCL>builder();
    for (String c : constraints) {

      ExpressionInOCL expression = org.cloudiator.ocl.OCLHelper.getOcl()
          .createInvariant(CloudiatorPackage.eINSTANCE.getComponent(), c);
      builder.add(expression);

    }
    this.constraints = builder.build();
  }

  public static OclCsp ofConstraints(Collection<String> constraints) throws ParserException {
    return new OclCsp(constraints);
  }

  public static OclCsp ofRequirements(Collection<RepresentableAsOCL> requirements)
      throws ParserException {
    Collection<String> constraints = Lists.newArrayList();
    for (RepresentableAsOCL representableAsOCL : requirements) {
      constraints.addAll(representableAsOCL.getOCLConstraints());
    }
    return new OclCsp(constraints);
  }

  public Set<String> getUnparsedConstraints() {
    return unparsedConstraints;
  }

  public Set<ExpressionInOCL> getConstraints() {
    return constraints;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("constraints", constraints).toString();
  }
}
