package org.cloudiator.ocl;

import static com.google.common.base.Preconditions.checkState;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.Node;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;

public class NodeCandidate implements Comparable<NodeCandidate> {

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorFactory.eINSTANCE;
  private final Hardware hardware;
  private final Image image;
  private final Location location;
  private final Cloud cloud;
  @Nullable
  private Double price = null;

  public NodeCandidate(Cloud cloud, Hardware hardware,
      Image image, Location location, @Nullable Double price) {
    this.cloud = cloud;
    this.hardware = hardware;
    this.image = image;
    this.location = location;
    this.price = price;
  }

  private Node toNode() {
    Node node = CLOUDIATOR_FACTORY.createNode();
    node.setImage(image);
    node.setHardware(hardware);
    node.setLocation(location);
    node.setCloud(cloud);
    node.setPrice(price);
    return node;
  }

  public Node getNode() {
    return toNode();
  }

  public Double getPrice() {
    checkState(price != null, "price not set");
    return price;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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

  public String toString() {
    return MoreObjects.toStringHelper(this).add("hardware", hardware).add("image", image)
        .add("location", location).add("price", price).toString();
  }

  @Override
  public int compareTo(NodeCandidate o) {
    return this.getPrice().compareTo(o.getPrice());
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

  public static class NodeCandidateFactory {

    private NodeCandidateFactory() {
    }

    public static NodeCandidateFactory create() {
      return new NodeCandidateFactory();
    }

    public NodeCandidate of(Cloud cloud, Hardware hardware, Image image, Location location,
        @Nullable Double price) {
      return new NodeCandidate(cloud, hardware, image, location, price);
    }
  }


}
