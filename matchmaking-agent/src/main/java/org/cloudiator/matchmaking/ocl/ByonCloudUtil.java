package org.cloudiator.matchmaking.ocl;

import cloudiator.Hardware;
import cloudiator.Image;
import cloudiator.Location;
import java.util.UUID;
import cloudiator.CloudiatorFactory;
import cloudiator.Api;
import cloudiator.CloudConfiguration;
import cloudiator.CloudCredential;

public class ByonCloudUtil {
  public static String BYON_PREFIX = "BYON_";
  private static String id = BYON_PREFIX + UUID.randomUUID().toString();
  private static final Api BYON_API = CloudiatorFactory.eINSTANCE.createApi();
  private static final CloudConfiguration BYON_CC = CloudiatorFactory.eINSTANCE.createCloudConfiguration();
  private static final CloudCredential BYON_CREDENTIAL = CloudiatorFactory.eINSTANCE.createCloudCredential();

  static {
    BYON_API.setProviderName("BYON");
    BYON_CC.setNodeGroup("byon");
    BYON_CREDENTIAL.setUser("byon");
    BYON_CREDENTIAL.setSecret("byon");
  }

  private ByonCloudUtil() {
    throw new IllegalStateException("Do not instantiate.");
  }

  public static String getId() {
    return id;
  }

  public static boolean isByon(String checkedId) {
    return id.equals(checkedId);
  }

  public static Api getByonApi() {
    return BYON_API;
  }

  public static CloudConfiguration getByonCloudConfiguration() {
    return BYON_CC;
  }

  public static CloudCredential getByonCredential() {
    return BYON_CREDENTIAL;
  }

  public static boolean isValidByonCombination(Image image, Hardware hardware, Location location) {
    String[] partsImage = image.getId().split("_");
    String[] partsHardware = hardware.getId().split("_");
    String[] partsLocation = location.getId().split("_");

    if(partsImage.length != 4 || partsHardware.length != 4 || partsLocation.length != 4) {
      return false;
    }

    if(!buildPrefixString(partsImage[0], partsImage[1], partsImage[2]).equals(ByonImage.ID_PREF)) {
      return false;
    }

    if(!buildPrefixString(partsHardware[0], partsHardware[1], partsHardware[2]).equals(ByonHardware.ID_PREF)) {
      return false;
    }

    if(!buildPrefixString(partsLocation[0], partsLocation[1], partsLocation[2]).equals(ByonLocation.ID_PREF)) {
      return false;
    }

    // valid combo if id postfixes are equal
    final String imageId = partsImage[3];
    final String hardwareId = partsHardware[3];
    final String locationId = partsLocation[3];

    if(imageId.equals(hardwareId) && imageId.equals(locationId)) {
      return true;
    }

    return false;
  }

  private static String buildPrefixString(String part1, String part2, String part3) {
    return part1 + "_" + part2 + "_" + part3 + "_";
  }

  public static class ByonImage {
    public static String ID_PREF = ByonCloudUtil.BYON_PREFIX  + "IMAGE_ID_";
    public static String PROV_ID_PREF = ByonCloudUtil.BYON_PREFIX  + "IMAGE_PROV_ID_";
    public static String NAME_PREF = ByonCloudUtil.BYON_PREFIX + "IMAGE_";
  }

  public static class ByonHardware {
    public static String ID_PREF = ByonCloudUtil.BYON_PREFIX  + "HW_ID_";
    public static String PROV_ID_PREF = ByonCloudUtil.BYON_PREFIX  + "HW_PROV_ID_";
    public static String NAME_PREF = ByonCloudUtil.BYON_PREFIX + "HW_";
  }

  public static class ByonLocation {
    public static String ID_PREF = ByonCloudUtil.BYON_PREFIX  + "LOCATION_ID_";
    public static String PROV_ID_PREF = ByonCloudUtil.BYON_PREFIX  + "LOCATION_PROV_ID_";
    public static String NAME_PREF = ByonCloudUtil.BYON_PREFIX + "EMPTY_LOCATION_NAME_";
  }
}
