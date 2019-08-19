package org.cloudiator.matchmaking.experiment;

import cloudiator.Api;
import cloudiator.Cloud;
import cloudiator.CloudConfiguration;
import cloudiator.CloudCredential;
import cloudiator.CloudType;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.GeoLocation;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.OSArchitecture;
import cloudiator.OSFamily;
import cloudiator.OperatingSystem;
import cloudiator.Price;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.cloudiator.matchmaking.ocl.ModelGenerator;
import org.cloudiator.matchmaking.ocl.PriceFunction;

public class SmallExperimentModelGenerator implements ModelGenerator {

  private static final CloudiatorModel CLOUDIATOR_MODEL = CloudiatorFactory.eINSTANCE
      .createCloudiatorModel();
  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorFactory.eINSTANCE;
  private static final PriceFunction PRICE_FUNCTION = new ExperimentModelPriceFunction();

  private static final int NR = 1;
  static final double DISK = 1000d;


  @Override
  public CloudiatorModel generateModel(String userId) {

    for (int i = 0; i < NR; i++) {
      Cloud cloud = CLOUDIATOR_FACTORY.createCloud();
      cloud.setId(String.valueOf(i));
      cloud.setType(CloudType.PUBLIC);
      cloud.setEndpoint("http://cloud" + cloud.getId());
      CloudCredential cloudCredential = CLOUDIATOR_FACTORY.createCloudCredential();
      cloudCredential.setUser(userId);
      cloudCredential.setSecret(userId);
      cloud.setCloudcredential(cloudCredential);
      Api api = CLOUDIATOR_FACTORY.createApi();
      api.setProviderName(cloud.getId());
      cloud.setApi(api);
      CloudConfiguration cloudConfiguration = CLOUDIATOR_FACTORY.createCloudConfiguration();
      cloudConfiguration.setNodeGroup("cloudiator");
      cloud.setConfiguration(cloudConfiguration);

      LocationGenerator locationGenerator = new LocationGenerator(cloud);
      for (Location location : locationGenerator.get()) {
        ImageGenerator imageGenerator = new ImageGenerator(cloud, location);
        HardwareGenerator hardwareGenerator = new HardwareGenerator(cloud, location);
        final List<Image> images = imageGenerator.get();
        final List<Hardware> hardwares = hardwareGenerator.get();

        cloud.getLocations().add(location);
        cloud.getHardwareList().addAll(hardwares);
        cloud.getImages().addAll(images);

        for (Image image : images) {
          for (Hardware hardware : hardwares) {
            Price price = CLOUDIATOR_FACTORY.createPrice();
            price.setHardware(hardware);
            price.setImage(image);
            price.setLocation(location);
            price.setPrice(PRICE_FUNCTION.calculatePricing(cloud, hardware, location, image));
            cloud.getPrices().add(price);
          }
        }
      }

      CLOUDIATOR_MODEL.getClouds().add(cloud);

    }

    return CLOUDIATOR_MODEL;
  }

  public static class LocationGenerator implements Supplier<List<Location>> {

    public static final Map<String, Double> OPTIONS = new HashMap<String, Double>() {{
      put("DE", 1.00);
      put("US", 1.01);
      put("CH", 1.02);
      put("AU", 1.03);
      put("IE", 1.04);
      put("JP", 1.05);
      put("BE", 1.06);
      put("SG", 1.07);
      put("GB", 1.08);
      put("ZA", 1.09);
      put("PL", 1.10);
      put("NO", 1.11);
      put("KR", 1.12);
      put("CA", 1.13);
      put("IN", 1.14);
    }};
    private final Cloud cloud;

    private LocationGenerator(Cloud cloud) {
      this.cloud = cloud;
    }

    @Override
    public List<Location> get() {

      List<Location> locations = new ArrayList<>();

      for (String country : OPTIONS.keySet()) {
        Location location = CLOUDIATOR_FACTORY.createLocation();
        GeoLocation geoLocation = CLOUDIATOR_FACTORY.createGeoLocation();
        location.setName(country);
        location.setProviderId(country);
        location.setId(cloud.getId() + ":" + location.getProviderId());
        geoLocation.setCountry(country);
        geoLocation.setCity("Ulm");
        geoLocation.setLatitude(5.1234);
        geoLocation.setLongitude(5.1234);
        location.setGeoLocation(geoLocation);
        locations.add(location);
        location.setAssignable(true);
      }
      return locations;
    }
  }

  private static class HardwareGenerator implements Supplier<List<Hardware>> {

    private static final List<Integer> CORE_OPTIONS = new ArrayList<Integer>() {{
      add(1);
      add(2);
      add(4);
      add(8);
    }};
    private static final List<Integer> RAM_OPTIONS = new ArrayList<Integer>() {{
      add(512);
      add(1024);
      add(2048);
      add(4096);
    }};

    private final Cloud cloud;
    private final Location location;

    private HardwareGenerator(Cloud cloud, Location location) {
      this.cloud = cloud;
      this.location = location;
    }

    @Override
    public List<Hardware> get() {

      List<Hardware> hardwareList = new ArrayList<>();

      for (int i = 0; i < CORE_OPTIONS.size(); i++) {
        Integer cores = CORE_OPTIONS.get(i);
        Integer ram = RAM_OPTIONS.get(i);
        Hardware hardware = CLOUDIATOR_FACTORY.createHardware();
        hardware.setProviderId(String.format("%s cores - %s ram", cores, ram));
        hardware.setName(hardware.getProviderId());
        hardware.setId(
            cloud.getId() + ":" + location.getProviderId() + ":" + hardware.getProviderId());
        hardware.setCores(cores);
        hardware.setRam(ram);
        hardware.setDisk(DISK);
        hardware.setLocation(location);
        hardwareList.add(hardware);
      }

      return hardwareList;
    }
  }

  private static class ImageGenerator implements Supplier<List<Image>> {

    private static final Map<String, OSFamily> OPTIONS = new HashMap<String, OSFamily>() {{
      put("ubuntu", OSFamily.UBUNTU);
      //put("debian", OSFamily.DEBIAN);
      //put("rhel", OSFamily.RHEL);
    }};
    private final Cloud cloud;
    private final Location location;

    private ImageGenerator(Cloud cloud, Location location) {
      this.cloud = cloud;
      this.location = location;
    }

    @Override
    public List<Image> get() {

      List<Image> images = new ArrayList<>();

      for (Map.Entry<String, OSFamily> entry : OPTIONS.entrySet()) {
        Image image = CLOUDIATOR_FACTORY.createImage();
        image.setProviderId(entry.getKey());
        image.setName(entry.getKey());
        image.setId(cloud.getId() + ":" + location.getProviderId() + ":" + image.getProviderId());
        image.setLocation(location);

        OperatingSystem os = CLOUDIATOR_MODEL.getOperatingsystems().stream()
            .filter(operatingSystem -> operatingSystem.getFamily().equals(entry.getValue()))
            .findAny().orElseGet(
                () -> {
                  OperatingSystem newOs = CLOUDIATOR_FACTORY.createOperatingSystem();
                  newOs.setFamily(entry.getValue());
                  newOs.setVersion(1);
                  newOs.setArchitecture(OSArchitecture.AMD64);
                  CLOUDIATOR_MODEL.getOperatingsystems().add(newOs);
                  return newOs;
                });
        image.setOperatingSystem(os);
        images.add(image);
      }

      return images;
    }
  }

}
