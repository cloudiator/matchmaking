package org.cloudiator.matchmaking.domain;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.Environment;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.Node;
import cloudiator.NodeType;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.ocl.ByonGenerator;

public class NodeCandidate implements Comparable<NodeCandidate> {

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorFactory.eINSTANCE;
  private String id;
  private NodeType type;
  private Cloud cloud;
  private Location location;
  // IAAS
  private Hardware hardware;
  private Image image;
  @Nullable
  private Double price = null;
  // FAAS
  private double pricePerInvocation;
  private double memoryPrice;
  private Environment environment;
  private static final NodeCandidateIdGenerator ID_GENERATOR = new HashingNodeCandidateIdGenerator();

  public NodeCandidate(Cloud cloud, Hardware hardware,
      Image image, Location location, @Nullable Double price) {
    this.type = NodeType.IAAS;
    this.cloud = cloud;
    this.hardware = hardware;
    this.image = image;
    this.location = location;
    this.price = price;
  }

  public NodeCandidate(String id, Hardware hardware,
      Image image, Location location) {
    this.id = id;
    this.type = NodeType.BYON;
    this.cloud = ByonGenerator.BYON_CLOUD;
    this.hardware = hardware;
    this.image = image;
    this.location = location;
    this.price = 0.0;
  }


  public NodeCandidate(Cloud cloud, Location location, Hardware hardware, double pricePerInvocation,
      double memoryPrice, Environment environment) {
    this.type = NodeType.FAAS;
    this.cloud = cloud;
    this.hardware = hardware;
    this.location = location;
    this.pricePerInvocation = pricePerInvocation;
    this.memoryPrice = memoryPrice;
    this.environment = environment;
  }

  private Node toNode() {
    Node node = CLOUDIATOR_FACTORY.createNode();
    node.setId(id());
    node.setType(type);
    node.setImage(image);
    node.setHardware(hardware);
    node.setLocation(location);
    node.setCloud(cloud);
    node.setPrice(price);
    node.setPricePerInvocation(pricePerInvocation);
    node.setMemoryPrice(memoryPrice);
    node.setEnvironment(environment);
    return node;
  }

  public synchronized String id() {
    if (id == null) {
      id = ID_GENERATOR.generateId(this);
    }
    return id;
  }

  public Node getNode() {
    return toNode();
  }

  public NodeType getType() {
    return type;
  }

  public Double getPrice() {
    if (price == null) {
      return 0.;
    }
    //checkState(price != null, "price not set");
    return price;
  }

  public Hardware getHardware() {
    return hardware;
  }

  public Image getImage() {
    return image;
  }

  public Location getLocation() {
    return location;
  }

  public Cloud getCloud() {
    return cloud;
  }

  public double getPricePerInvocation() {
    return pricePerInvocation;
  }

  public double getMemoryPrice() {
    return memoryPrice;
  }

  public Environment getEnvironment() {
    return environment;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((hardware == null) ? 0 : hardware.hashCode());
    result = prime * result + ((image == null) ? 0 : image.hashCode());
    result = prime * result + ((location == null) ? 0 : location.hashCode());
    result = prime * result + ((cloud == null) ? 0 : cloud.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NodeCandidate other = (NodeCandidate) obj;
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    if (cloud == null) {
      if (other.cloud != null) {
        return false;
      }
    } else if (!cloud.equals(other.cloud)) {
      return false;
    }
    if (hardware == null) {
      if (other.hardware != null) {
        return false;
      }
    } else if (!hardware.equals(other.hardware)) {
      return false;
    }
    if (image == null) {
      if (other.image != null) {
        return false;
      }
    } else if (!image.equals(other.image)) {
      return false;
    }
    if (location == null) {
      if (other.location != null) {
        return false;
      }
    } else if (!location.equals(other.location)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("hardware", hardware).add("image", image)
        .add("location", location).add("price", price).toString();
  }

  @Override
  public int compareTo(NodeCandidate o) {
    return this.getPrice().compareTo(o.getPrice());
  }

  public static class NodeCandidateFactory {

    private NodeCandidateFactory() {
    }

    public static NodeCandidateFactory create() {
      return new NodeCandidateFactory();
    }

    public NodeCandidate of(Cloud cloud, Hardware hardware, Image image,
        Location location, @Nullable Double price) {
      return new NodeCandidate(cloud, hardware, image, location, price);
    }

    public NodeCandidate byon(String id, Hardware hardware, Image image,
        Location location) {
      return new NodeCandidate(id, hardware, image, location);
    }

    public NodeCandidate of(Cloud cloud, Location location, Hardware hardware,
        double pricePerInvocation, double memoryPrice, Environment environment) {
      return new NodeCandidate(cloud, location, hardware, pricePerInvocation, memoryPrice,
          environment);
    }
  }
}
