package org.cloudiator.matchmaking.byon;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.Hardware;
import java.util.Set;
import java.util.UUID;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.messages.Node;

public class NodeToNodeCandidate {

  private static Cloud BYON_CLOUD =

  static {
    final Cloud cloud = CloudiatorFactory.eINSTANCE.createCloud();
    BYON_CLOUD = cloud;
  }

  private NodeToNodeCandidate() {
  }

  public Set<NodeCandidate> from(Node node) {

    Hardware hardware = CloudiatorFactory.eINSTANCE.createHardware();
    hardware.setId("BYON"+ UUID.randomUUID().toString());
    hardware.setProviderId("BYON"+ UUID.randomUUID().toString());
    hardware.setCores(4);
    BYON_CLOUD.getHardwareList().add(hardware);


  }


}
