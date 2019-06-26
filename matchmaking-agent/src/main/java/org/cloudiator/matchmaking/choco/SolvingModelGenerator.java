package org.cloudiator.matchmaking.choco;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.Node;
import cloudiator.Price;
import de.uniulm.omi.cloudiator.util.StreamUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.cloudiator.matchmaking.LocationUtil;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.ocl.NodeCandidates;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class SolvingModelGenerator implements Function<NodeCandidates, CloudiatorModel> {

  private static class SolvingModelGeneratorInternal {

    private final NodeCandidates nodeCandidates;
    private final CloudiatorModel model;

    private SolvingModelGeneratorInternal(NodeCandidates nodeCandidates) {
      this.nodeCandidates = nodeCandidates;
      this.model = CloudiatorFactory.eINSTANCE.createCloudiatorModel();
    }

    private CloudiatorModel generate() {
      importNodeCandidates();
      cleanUnneededObjects();
      handleOperatingSystems();
      return model;
    }

    private void handleOperatingSystems() {
      model.getClouds().stream().flatMap(
          (Function<Cloud, Stream<Image>>) cloud -> cloud.getImages().stream()).map(
          Image::getOperatingSystem).forEach(
          operatingSystem -> model.getOperatingsystems().add(operatingSystem));
    }

    private void cleanUnneededObjects() {

      Set<String> imageIds = new HashSet<>();
      Set<String> locationIds = new HashSet<>();
      Set<String> hardwareIds = new HashSet<>();

      for (NodeCandidate nodeCandidate : nodeCandidates) {
        imageIds.add(nodeCandidate.getImage().getId());
        locationIds.add(nodeCandidate.getLocation().getId());

        LocationUtil.parents(nodeCandidate.getLocation()).stream().map(Location::getId)
            .forEach(locationIds::add);

        hardwareIds.add(nodeCandidate.getHardware().getId());
      }

      model.getClouds().forEach(cloud -> {

        Set<Image> imagesToBeRemoved = new HashSet<>();
        Set<Location> locationsToBeRemoved = new HashSet<>();
        Set<Hardware> hardwareToBeRemoved = new HashSet<>();
        Set<Price> pricesToBeRemoved = new HashSet<>();

        cloud.getImages().forEach(image -> {
          if (!imageIds.contains(image.getId())) {
            imagesToBeRemoved.add(image);
          }
        });

        cloud.getLocations().forEach(location -> {
          if (!locationIds.contains(location.getId())) {
            locationsToBeRemoved.add(location);
          }
        });

        cloud.getHardwareList().forEach(hardware -> {
          if (!hardwareIds.contains(hardware.getId())) {
            hardwareToBeRemoved.add(hardware);
          }
        });

        //derive to be deleted prices
        for (Price price : cloud.getPrices()) {
          if (!imageIds.contains(price.getImage().getId())) {
            pricesToBeRemoved.add(price);
            continue;
          }
          if (!hardwareIds.contains(price.getHardware().getId())) {
            pricesToBeRemoved.add(price);
            continue;
          }
          if (!locationIds.contains(price.getLocation().getId())) {
            pricesToBeRemoved.add(price);
          }
        }

        //remove everything
        cloud.getImages().removeAll(imagesToBeRemoved);
        cloud.getHardwareList().removeAll(hardwareToBeRemoved);
        cloud.getLocations().removeAll(locationsToBeRemoved);
        cloud.getPrices().removeAll(pricesToBeRemoved);


      });


    }

    private void importNodeCandidates() {
      nodeCandidates.stream().map(NodeCandidate::getCloud).distinct().forEach(
          new Consumer<Cloud>() {
            @Override
            public void accept(Cloud cloud) {
              model.getClouds().add(EcoreUtil.copy(cloud));
            }
          });

      nodeCandidates.forEach(nodeCandidate -> {
        final Node node = CloudiatorFactory.eINSTANCE.createNode();

        Cloud cloud = model.getClouds().stream().filter(
            existingCloud -> nodeCandidate.getCloud().getId().equals(existingCloud.getId()))
            .collect(StreamUtil.getOnly()).orElseThrow(
                () -> new IllegalStateException(
                    "Could not find cloud with id " + node.getCloud().getId()));

        Image image = model.getClouds().stream()
            .flatMap(existingCloud -> existingCloud.getImages().stream()).filter(
                existingImage -> nodeCandidate.getImage().getId().equals(existingImage.getId()))
            .collect(StreamUtil.getOnly()).orElseThrow(
                () -> new IllegalStateException(
                    "Could not find image with id " + node.getImage().getId()));

        Location location = model.getClouds().stream()
            .flatMap(existingCloud -> existingCloud.getLocations().stream()).filter(
                existingLocation -> nodeCandidate.getLocation().getId()
                    .equals(existingLocation.getId()))
            .collect(StreamUtil.getOnly()).orElseThrow(
                () -> new IllegalStateException(
                    "Could not find location with id " + node.getLocation().getId()));

        Hardware hardware = model.getClouds().stream()
            .flatMap(existingCloud -> existingCloud.getHardwareList().stream()).filter(
                existingHardware -> nodeCandidate.getHardware().getId()
                    .equals(existingHardware.getId()))
            .collect(StreamUtil.getOnly()).orElseThrow(
                () -> new IllegalStateException(
                    "Could not find hardware with id " + node.getHardware().getId()));

        node.setCloud(cloud);
        node.setHardware(hardware);
        node.setLocation(location);
        node.setImage(image);

        model.getNodes().add(node);
      });
    }

  }

  @Override
  public CloudiatorModel apply(NodeCandidates nodeCandidates) {

    return new SolvingModelGeneratorInternal(nodeCandidates).generate();
  }
}
