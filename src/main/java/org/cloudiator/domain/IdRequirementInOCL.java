package org.cloudiator.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;

public class IdRequirementInOCL implements IdRequirement, RepresentableAsOCL {

  private final IdRequirement delegate;

  private final static String TEMPLATE = "nodes->exists(%s = '%s')";
  private final static String HARDWARE_TEMPLATE = String.format(TEMPLATE, "hardware.id", "%s");
  private final static String LOCATION_TEMPLATE = String.format(TEMPLATE, "location.id", "%s");
  private final static String IMAGE_TEMPLATE = String.format(TEMPLATE, "image.id", "%s");

  public IdRequirementInOCL(IdRequirement delegate) {
    checkNotNull(delegate, "delegate is null");
    this.delegate = delegate;
  }

  @Override
  public String hardwareId() {
    return delegate.hardwareId();
  }

  @Override
  public String locationId() {
    return delegate.locationId();
  }

  @Override
  public String imageId() {
    return delegate.imageId();
  }

  @Override
  public Set<String> getOCLConstraints() {

    Set<String> constraints = Sets.newHashSetWithExpectedSize(3);
    constraints.add(String.format(HARDWARE_TEMPLATE, hardwareId()));
    constraints.add(String.format(LOCATION_TEMPLATE, locationId()));
    constraints.add(String.format(IMAGE_TEMPLATE, imageId()));

    return ImmutableSet.copyOf(constraints);
  }
}
