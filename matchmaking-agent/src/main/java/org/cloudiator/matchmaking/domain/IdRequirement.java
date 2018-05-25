package org.cloudiator.matchmaking.domain;

public interface IdRequirement extends Requirement {

  static IdRequirement of(String hardwareId, String locationId, String imageId) {
    return new IdRequirementInOCL(new IdRequirementImpl(hardwareId, locationId, imageId));
  }

  String hardwareId();

  String locationId();

  String imageId();
}
