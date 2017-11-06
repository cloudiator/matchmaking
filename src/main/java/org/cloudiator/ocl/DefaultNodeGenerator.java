package org.cloudiator.ocl;

import static com.google.common.base.Preconditions.checkNotNull;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.CloudiatorPackage;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.Price;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.cloudiator.ocl.NodeCandidate.NodeCandidateFactory;

public class DefaultNodeGenerator implements NodeGenerator {

  private final NodeCandidateFactory nodeCandidateFactory;
  private final CloudiatorModel cloudiatorModel;
  private final CloudiatorFactory cloudiatorFactory = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private final Random random = new Random();

  public DefaultNodeGenerator(NodeCandidateFactory nodeCandidateFactory,
      CloudiatorModel cloudiatorModel) {
    this.nodeCandidateFactory = nodeCandidateFactory;
    this.cloudiatorModel = cloudiatorModel;
  }

  private Price generatePrice(Cloud cloud, Hardware hardware, Image image, Location location) {

    double price = 0;
    price += hardware.getCores().doubleValue();
    price += hardware.getRam().doubleValue() / 1000;

    //double locationPriceFactor = 1 + random.nextDouble();
    //price = price * locationPriceFactor;

   // double cloudPriceFactor = 1 + random.nextDouble();

    Price modelPrice = cloudiatorFactory.createPrice();
    modelPrice.setHardware(hardware);
    modelPrice.setImage(image);
    modelPrice.setLocation(location);
    modelPrice.setPrice(price);
    return modelPrice;
  }

  /*
   * (non-Javadoc)
   *
   * @see ocl.NodeGenerator#getPossibleNodes()
   */
  @Override
  public Set<NodeCandidate> getPossibleNodes() {
    Set<NodeCandidate> nodeCandidates = new HashSet<>();
    for (Cloud cloud : cloudiatorModel.getClouds()) {
      for (Image image : cloud.getImages()) {
        for (Hardware hardware : cloud.getHardwareList()) {
          for (Location location : cloud.getLocations()) {
            //check if valid combination
            if (isValidCombination(image, hardware, location)) {
              generatePrice(cloud, hardware, image, location);
              nodeCandidates.add(nodeCandidateFactory.of(cloud, hardware, image, location));
            }
          }
        }
      }
    }
    System.out
        .println(String.format("%s generated all possible nodes: %s", this, nodeCandidates.size()));
    return nodeCandidates;
  }

  private static boolean isValidCombination(Image image, Hardware hardware, Location location) {
    checkNotNull(location, "location is null");
    String imageLocationId = null;
    if (image.getLocation() != null) {
      imageLocationId = image.getLocation().getId();
    }

    String hardwareLocationId = null;
    if (hardware.getLocation() != null) {
      hardwareLocationId = hardware.getLocation().getId();
    }

    return location.getId().equals(imageLocationId) && location.getId().equals(hardwareLocationId);
  }

}
