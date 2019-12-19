package org.cloudiator.matchmaking.ocl;

import cloudiator.Api;
import cloudiator.Cloud;
import cloudiator.CloudConfiguration;
import cloudiator.CloudCredential;
import cloudiator.CloudState;
import cloudiator.CloudType;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.GeoLocation;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.LocationScope;
import cloudiator.OSArchitecture;
import cloudiator.OSFamily;
import cloudiator.OperatingSystem;
import cloudiator.Price;
import java.util.HashSet;
import java.util.UUID;

public class ExampleModel {

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorFactory.eINSTANCE;


  public static CloudiatorModel testModel() {

    final CloudiatorModel cloudiatorModel = CLOUDIATOR_FACTORY.createCloudiatorModel();
    cloudiatorModel.getOperatingsystems().add(generateOS());

    Iterable<CloudType> types = new HashSet<CloudType>(){{
      add(CloudType.PRIVATE);
      add(CloudType.PUBLIC);
    }};

    for(CloudType type : types) {
      final Cloud cloud = generateCloud(type);
      cloudiatorModel.getClouds().add(cloud);

      generateLocations(cloud);
      generateHardware(cloud);
      generateImages(cloudiatorModel);
      generatePrices(cloud);
    }

    return cloudiatorModel;
  }

  private static void generatePrices(Cloud cloud) {
    final HardwareBasedPriceFunction hardwareBasedPriceFunction = new HardwareBasedPriceFunction();
    for (Hardware hardware : cloud.getHardwareList()) {
      for (Image image : cloud.getImages()) {
        for (Location location : cloud.getLocations()) {
          final Double price = hardwareBasedPriceFunction
              .calculatePricing(cloud, hardware, location, image, null).get();
          final Price modelPrice = CLOUDIATOR_FACTORY.createPrice();
          modelPrice.setHardware(hardware);
          modelPrice.setImage(image);
          modelPrice.setLocation(location);
          modelPrice.setPrice(price);
          cloud.getPrices().add(modelPrice);
        }
      }
    }
  }

  private static void generateHardware(Cloud cloud) {

    final int[] cores = new int[]{1, 2, 4, 8, 16};
    final int[] rams = new int[]{1024, 2048, 4096};
    final int[] disks = new int[]{10, 20, 40, 100};

    for (Location location : cloud.getLocations()) {
      final Hardware hardware = CLOUDIATOR_FACTORY.createHardware();

      for (int core : cores) {
        for (int ram : rams) {
          for (int disk : disks) {

            final String uniqueFromProperties = String
                .format("%s-%s-%s-%s-%s", cloud.getId(), location.getId(), core, ram, disk);

            hardware.setCores(core);
            hardware.setRam(ram);
            hardware.setId(uniqueFromProperties);
            hardware.setName(uniqueFromProperties);
            hardware.setProviderId(uniqueFromProperties);
            hardware.setLocation(location);
          }
        }
      }
      cloud.getHardwareList().add(hardware);
    }
  }

  private static OperatingSystem generateOS() {
    final OperatingSystem operatingSystem = CLOUDIATOR_FACTORY.createOperatingSystem();
    operatingSystem.setArchitecture(OSArchitecture.AMD64);
    operatingSystem.setFamily(OSFamily.UBUNTU);
    operatingSystem.setVersion(1804);
    return operatingSystem;
  }

  private static void generateLocations(Cloud cloud) {
    String[] countries = new String[]{"DE", "US", null};

    for (String country : countries) {

      final Location location = CLOUDIATOR_FACTORY.createLocation();
      location.setLocationScope(LocationScope.REGION);
      location.setAssignable(true);

      final UUID uuid = UUID.randomUUID();

      location.setId(uuid.toString());
      location.setProviderId(uuid.toString());
      location.setName(uuid.toString());
      location.setParent(null);

      if (country != null) {
        final GeoLocation geoLocation = CLOUDIATOR_FACTORY.createGeoLocation();
        geoLocation.setCity(null);
        geoLocation.setCountry(country);
        geoLocation.setLatitude(null);
        geoLocation.setLongitude(null);
        location.setGeoLocation(geoLocation);
      }
      cloud.getLocations().add(location);
    }

  }

  private static void generateImages(CloudiatorModel cloudiatorModel) {

    for (OperatingSystem os : cloudiatorModel.getOperatingsystems()) {
      for (Cloud cloud : cloudiatorModel.getClouds()) {
        for (Location location : cloud.getLocations()) {
          final Image image = CLOUDIATOR_FACTORY.createImage();
          final UUID uuid = UUID.randomUUID();
          image.setId(uuid.toString());
          image.setProviderId(uuid.toString());
          image.setName(uuid.toString());
          image.setLocation(location);
          image.setOperatingSystem(os);
          cloud.getImages().add(image);
        }
      }
    }

  }


  private static Cloud generateCloud(CloudType cloudType) {
    final Cloud cloud = CLOUDIATOR_FACTORY.createCloud();
    cloud.setId(UUID.randomUUID().toString());
    cloud.setEndpoint("http://example.com/api");
    cloud.setState(CloudState.OK);
    cloud.setType(cloudType);
    cloud.setDiagnostic(null);
    cloud.setCloudcredential(generateCredential());
    cloud.setApi(generateApi());
    cloud.setConfiguration(generateConfiguration());
    return cloud;
  }

  private static Api generateApi() {
    final Api api = CLOUDIATOR_FACTORY.createApi();
    api.setProviderName("openstack4j");
    return api;
  }

  private static CloudCredential generateCredential() {
    final CloudCredential cloudCredential = CLOUDIATOR_FACTORY.createCloudCredential();
    cloudCredential.setSecret("verySecurePassword");
    cloudCredential.setUser("john.doe@example.com");
    return cloudCredential;
  }

  private static CloudConfiguration generateConfiguration() {
    final CloudConfiguration cloudConfiguration = CLOUDIATOR_FACTORY.createCloudConfiguration();
    cloudConfiguration.setNodeGroup("cloudiator");
    return cloudConfiguration;
  }


}
