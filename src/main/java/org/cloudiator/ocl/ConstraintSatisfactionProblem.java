package org.cloudiator.ocl;

import com.google.common.base.MoreObjects;
import java.util.Set;

public class ConstraintSatisfactionProblem {

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

  public Set<String> constraints;

  public ConstraintSatisfactionProblem(Set<String> constraints) {
    this.constraints = constraints;
  }

  public Set<String> getConstraints() {
    return constraints;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("constraints", constraints).toString();
  }
}
