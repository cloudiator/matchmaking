package org.cloudiator.matchmaking.ocl;

import java.util.HashSet;

public class OclTestCsp {

  private OclTestCsp() {
    throw new AssertionError("Do not instantiate");
  }

  public static final HashSet<String> TEST_CSP = new HashSet<String>() {{
    add("nodes->exists(location.geoLocation.country = 'DE')");
    add("nodes->forAll(n | n.hardware.cores >= 2)");
    add("nodes->isUnique(n | n.location.geoLocation.country)");
    add("nodes->forAll(n | n.hardware.ram >= 1024)");
    add("nodes->forAll(n | n.hardware.ram < 8000)");
    add("nodes->forAll(n | n.hardware.cores >= 4 implies n.hardware.ram >= 4096)");
    add("nodes->forAll(n | n.image.operatingSystem.family = OSFamily::UBUNTU)");
    add("nodes->select(n | n.hardware.cores >= 4)->size() = 2");
    add("nodes.hardware.cores->sum() >= 15");
    add("nodes->forAll(n | Set{'test','test2','test3'}->includes(n.location.geoLocation.country))");
  }};

}
