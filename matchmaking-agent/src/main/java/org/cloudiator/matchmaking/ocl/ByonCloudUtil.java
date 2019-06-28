package org.cloudiator.matchmaking.ocl;

import java.util.UUID;

public class ByonCloudUtil {
  private static String id = "BYON_"+ UUID.randomUUID().toString();

  private ByonCloudUtil() {
    throw new IllegalStateException("Do not instantiate.");
  }

  public static String getId() {
    return id;
  }

  public static boolean isByon(String checkedId) {
    return id.equals(checkedId);
  }
}
