package org.cloudiator.experiment;

import static com.google.common.base.Preconditions.checkNotNull;

import cloudiator.Api;
import cloudiator.Cloud;
import cloudiator.CloudConfiguration;
import cloudiator.CloudCredential;
import cloudiator.CloudType;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorModel;
import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import cloudiator.OSFamily;
import cloudiator.OperatingSystem;
import cloudiator.Price;
import io.github.cloudiator.cloudharmony.ApiClient;
import io.github.cloudiator.cloudharmony.ApiException;
import io.github.cloudiator.cloudharmony.api.ApiApi;
import io.github.cloudiator.cloudharmony.model.CloudService;
import io.github.cloudiator.cloudharmony.model.ComputeInstanceType;
import io.github.cloudiator.cloudharmony.model.ComputeInstanceTypePrice;
import io.github.cloudiator.cloudharmony.model.ComputeProperties;
import io.github.cloudiator.cloudharmony.model.MarketshareSnapshot.ServiceTypeEnum;
import io.github.cloudiator.cloudharmony.model.ServiceRegion;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.cloudiator.ocl.ModelGenerator;

public class CloudHarmony implements ModelGenerator {

  private static final ApiClient API_CLIENT = new ApiClient();
  private static final ApiApi API_API = new ApiApi(API_CLIENT);
  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorFactory.eINSTANCE;
  private static final CloudiatorModel CLOUDIATOR_MODEL = CLOUDIATOR_FACTORY
      .createCloudiatorModel();
  private static final Map<String, Image> images = generateImages();

  private List<String> services() throws ApiException {
    return new ArrayList<>(API_API
        .getServices(null, Collections.singletonList(ServiceTypeEnum.COMPUTE.toString()), null)
        .getIds());
  }

  private Set<CloudService> cloudServices() throws ApiException {
    return services().stream().map(s -> {
      try {
        return API_API.getService(s, null);
      } catch (ApiException e) {
        return null;
      }
    }).filter(cloudService -> {
      if (cloudService == null) {
        return false;
      }
      return true;
    }).collect(Collectors.toSet());
  }

  private static Image generateCloudAndLocationUniqueImage(Image image, Cloud cloud,
      Location location) {
    Image unique = CLOUDIATOR_FACTORY.createImage();
    unique.setId(image.getId() + "-" + cloud.getId() + "-" + location.getId());
    unique.setProviderId(image.getId());
    unique.setName(unique.getId());
    unique.setLocation(location);
    if (image.getOperatingSystem() != null) {
      unique.setOperatingSystem(image.getOperatingSystem());
    }
    return unique;
  }

  private static Map<String, Image> generateImages() {
    Map<String, Image> images = new HashMap<>();

    Image ubuntu = CLOUDIATOR_FACTORY.createImage();
    ubuntu.setId("linux.ubuntu");
    ubuntu.setProviderId("linux.ubuntu");
    ubuntu.setName("linux.ubuntu");
    OperatingSystem ubuntuOs = CLOUDIATOR_FACTORY.createOperatingSystem();
    CLOUDIATOR_MODEL.getOperatingsystem().add(ubuntuOs);
    ubuntuOs.setFamily(OSFamily.UBUNTU);
    ubuntu.setOperatingSystem(ubuntuOs);
    images.put(ubuntu.getId(), ubuntu);

    Image windows2008 = CLOUDIATOR_FACTORY.createImage();
    windows2008.setId("windows.2008");
    windows2008.setProviderId("windows.2008");
    windows2008.setName("windows.2008");
    OperatingSystem windows2008Os = CLOUDIATOR_FACTORY.createOperatingSystem();
    CLOUDIATOR_MODEL.getOperatingsystem().add(windows2008Os);
    windows2008Os.setFamily(OSFamily.WINDOWS);
    windows2008Os.setVersion("2008");
    windows2008.setOperatingSystem(windows2008Os);
    images.put(windows2008.getId(), windows2008);

    Image suse = CLOUDIATOR_FACTORY.createImage();
    suse.setId("linux.suse");
    suse.setProviderId("linux.suse");
    suse.setName("linux.suse");
    OperatingSystem suseOs = CLOUDIATOR_FACTORY.createOperatingSystem();
    CLOUDIATOR_MODEL.getOperatingsystem().add(suseOs);
    suseOs.setFamily(OSFamily.SUSE);
    suse.setOperatingSystem(suseOs);
    images.put(suse.getId(), suse);

    Image freebsd = CLOUDIATOR_FACTORY.createImage();
    freebsd.setId("freeBSD");
    freebsd.setProviderId("freeBSD");
    freebsd.setName("freeBSD");
    OperatingSystem freebsdOs = CLOUDIATOR_FACTORY.createOperatingSystem();
    CLOUDIATOR_MODEL.getOperatingsystem().add(freebsdOs);
    freebsdOs.setFamily(OSFamily.FREEBSD);
    freebsd.setOperatingSystem(freebsdOs);
    images.put(freebsd.getId(), freebsd);

    Image windows2012 = CLOUDIATOR_FACTORY.createImage();
    windows2012.setId("windows.2012");
    windows2012.setProviderId("windows.2012");
    windows2012.setName("windows.2012");
    OperatingSystem windows2012Os = CLOUDIATOR_FACTORY.createOperatingSystem();
    CLOUDIATOR_MODEL.getOperatingsystem().add(windows2012Os);
    windows2012Os.setFamily(OSFamily.WINDOWS);
    windows2012Os.setVersion("2012");
    windows2012.setOperatingSystem(windows2012Os);
    images.put(windows2012.getId(), windows2012);

    Image debian = CLOUDIATOR_FACTORY.createImage();
    debian.setId("linux.debian");
    debian.setProviderId("linux.debian");
    debian.setName("linux.debian");
    OperatingSystem debianOs = CLOUDIATOR_FACTORY.createOperatingSystem();
    CLOUDIATOR_MODEL.getOperatingsystem().add(debianOs);
    debianOs.setFamily(OSFamily.DEBIAN);
    debian.setOperatingSystem(debianOs);
    images.put(debian.getId(), debian);

    Image gentoo = CLOUDIATOR_FACTORY.createImage();
    gentoo.setId("linux.gentoo");
    gentoo.setProviderId("linux.gentoo");
    gentoo.setName("linux.gentoo");
    OperatingSystem gentooOs = CLOUDIATOR_FACTORY.createOperatingSystem();
    CLOUDIATOR_MODEL.getOperatingsystem().add(gentooOs);
    gentooOs.setFamily(OSFamily.GENTOO);
    gentoo.setOperatingSystem(gentooOs);
    images.put(gentoo.getId(), gentoo);

    Image rhel = CLOUDIATOR_FACTORY.createImage();
    rhel.setId("linux.rhel");
    rhel.setProviderId("linux.rhel");
    rhel.setName("linux.rhel");
    OperatingSystem rhelOs = CLOUDIATOR_FACTORY.createOperatingSystem();
    CLOUDIATOR_MODEL.getOperatingsystem().add(rhelOs);
    rhelOs.setFamily(OSFamily.RHEL);
    rhel.setOperatingSystem(rhelOs);
    images.put(rhel.getId(), rhel);

    Image fedora = CLOUDIATOR_FACTORY.createImage();
    fedora.setId("linux.fedora");
    fedora.setProviderId("linux.fedora");
    fedora.setName("linux.fedora");
    OperatingSystem fedoraOs = CLOUDIATOR_FACTORY.createOperatingSystem();
    CLOUDIATOR_MODEL.getOperatingsystem().add(fedoraOs);
    fedoraOs.setFamily(OSFamily.FEDORA);
    fedora.setOperatingSystem(fedoraOs);
    images.put(fedora.getId(), fedora);

    Image centos = CLOUDIATOR_FACTORY.createImage();
    centos.setId("linux.centos");
    centos.setProviderId("linux.centos");
    centos.setName("linux.centos");
    OperatingSystem centosOs = CLOUDIATOR_FACTORY.createOperatingSystem();
    CLOUDIATOR_MODEL.getOperatingsystem().add(centosOs);
    centosOs.setFamily(OSFamily.CENTOS);
    centos.setOperatingSystem(centosOs);
    images.put(centos.getId(), centos);

    return images;
  }


  @Override
  public CloudiatorModel generateModel(String userId) {
    try {
      for (CloudService cloudService : cloudServices()) {
        Api api = CLOUDIATOR_FACTORY.createApi();
        api.setProviderName(cloudService.getName());
        CloudCredential cc = CLOUDIATOR_FACTORY.createCloudCredential();
        cc.setUser("cloudHarmony");
        cc.setSecret("cloudHarmonySecret");
        CloudConfiguration cloudConfiguration = CLOUDIATOR_FACTORY.createCloudConfiguration();
        cloudConfiguration.setNodeGroup("cloudiator");
        Cloud cloud = CLOUDIATOR_FACTORY.createCloud();
        cloud.setType(CloudType.PUBLIC);
        cloud.setId(cloudService.getServiceId());
        cloud.setApi(api);
        cloud.setCloudcredential(cc);
        cloud.setConfiguration(cloudConfiguration);

        String endpoint = null;
        if (cloudService.getLink() != null) {
          endpoint = cloudService.getLink();
        }
        if (endpoint == null && cloudService.getWebsite() != null) {
          endpoint = cloudService.getWebsite();
        }
        if (endpoint == null) {
          endpoint = String.format("https://%s", cloud.getApi().getProviderName());
        }
        cloud.setEndpoint(endpoint);

        System.out.println("Analyzing cloud: " + cloud);

        for (ServiceRegion serviceRegion : cloudService.getRegions()) {

          Map<String, Image> locationImages = new HashMap<>();

          System.out.println(
              "Analyzing service region " + serviceRegion.getProviderCode() + " of " + cloudService
                  .getRegions().size() + " service regions.");

          Location location = CLOUDIATOR_FACTORY.createLocation();
          location.setCity(serviceRegion.getCity());
          location.setCountry(serviceRegion.getCountry().toString());
          location.setLatitude(serviceRegion.getLocationLat().doubleValue());
          location.setLongitude(serviceRegion.getLocationLong().doubleValue());
          location.setId(cloud.getId() + serviceRegion.getProviderCode());
          location.setProviderId(serviceRegion.getProviderCode());
          cloud.getLocations().add(location);

          ComputeProperties computeProperties = computePropertiesForCloudServiceAndRegion(
              cloud.getId(), location.getProviderId());

          if (computeProperties != null) {
            System.out.println("Analyzing compute properties: " + computeProperties.getName());
            for (String instanceType : computeProperties.getInstanceTypes()) {

              System.out.println(
                  "Analyzing compute instance type: " + instanceType + " of " + computeProperties
                      .getInstanceTypes().size() + " instance types.");

              ComputeInstanceType computeInstanceType = computeInstanceType(cloud.getId(),
                  instanceType, location.getProviderId());

              if (computeInstanceType != null) {

                Hardware hardware = CLOUDIATOR_FACTORY.createHardware();
                hardware.setId(location.getId() + instanceType);
                hardware.setProviderId(instanceType);
                hardware.setCores(computeInstanceType.getCpuCores().toBigInteger());
                if (computeInstanceType.getLocalStorage() != null) {
                  hardware.setDisk(computeInstanceType.getLocalStorage().floatValue());
                }
                hardware.setRam(
                    computeInstanceType.getMemory().toBigInteger()
                        .multiply(BigInteger.valueOf(1000)));
                hardware.setLocation(location);
                cloud.getHardwareList().add(hardware);

                for (String os : computeInstanceType.getOperatingSystems()) {
                  Image image = images.get(os);
                  if (image != null) {

                    Image cloudUniqueImage = generateCloudAndLocationUniqueImage(image, cloud,
                        location);
                    if (!locationImages.containsKey(cloudUniqueImage.getId())) {
                      locationImages.put(cloudUniqueImage.getId(), cloudUniqueImage);
                      cloud.getImages().add(cloudUniqueImage);
                    } else {
                      cloudUniqueImage = locationImages.get(cloudUniqueImage.getId());
                    }

                    //set the price
                    double pricing = selectPrice(
                        computeInstanceType, image.getId());

                    Price price = CLOUDIATOR_FACTORY.createPrice();
                    price.setImage(cloudUniqueImage);
                    price.setLocation(location);
                    price.setHardware(hardware);
                    price.setPrice(pricing);
                    cloud.getPrices().add(price);
                  }
                }
              }
            }
          }
        }

        CLOUDIATOR_MODEL.getClouds().add(cloud);
      }

      return CLOUDIATOR_MODEL;
    } catch (ApiException e) {
      throw new IllegalStateException(e);
    }


  }

  private double selectPrice(ComputeInstanceType computeInstanceType,
      String image) {

    TreeSet<ComputeInstanceTypePrice> prices = new TreeSet<>(
        Comparator.comparing(ComputeInstanceTypePrice::getNormalizedPrice));

    //if (BillIntervalEnum.HOUR.equals(computeInstanceTypePrice.getBillInterval())
    //    || PriceIntervalEnum.HOUR.equals(computeInstanceTypePrice.getPriceInterval())) {
    //  return true;
    //}
    //return false;
    computeInstanceType.getPricing().stream().filter(
        computeInstanceTypePrice -> computeInstanceTypePrice.getOperatingSystems()
            .contains(image)).forEach(prices::add);

    if (prices.isEmpty()) {
      return Double.MAX_VALUE;
    }
    return prices.first().getNormalizedPrice().doubleValue();

  }

  @Nullable
  private ComputeInstanceType computeInstanceType(String cloud, String typeId, String region) {
    try {
      return API_API
          .getComputeInstanceType(cloud, typeId, null,
              Collections.singletonList(region), null);
    } catch (Exception e) {
      return null;
    }
  }

  @Nullable
  private ComputeProperties computePropertiesForCloudServiceAndRegion(
      String cloud,
      String region) throws ApiException {

    checkNotNull(cloud);
    checkNotNull(region);

    return API_API.getComputeProperties(cloud, null, null).stream().filter(
        computeProperties -> {
          if (computeProperties == null) {
            return false;
          }
          if (computeProperties.getRegions() == null) {
            return false;
          }
          return computeProperties.getRegions().contains(region);
        }).findAny().orElse(null);
  }
}
