package org.cloudiator.matchmaking.ocl;

import cloudiator.Cloud;
import cloudiator.Image;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.converters.ImageConverter;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.ImageService;

public class ImageSupplier implements Supplier<Set<Image>> {

  private static final ImageConverter IMAGE_CONVERTER = new ImageConverter();
  private final ImageService imageService;
  private final String userId;
  private final Cloud cloud;

  public ImageSupplier(ImageService imageService, String userId, Cloud cloud) {
    this.imageService = imageService;
    this.userId = userId;
    this.cloud = cloud;
  }

  private ImageQueryRequest buildRequest() {
    return ImageQueryRequest.newBuilder().setCloudId(cloud.getId()).setUserId(userId).build();
  }

  @Override
  public Set<Image> get() {
    try {
      return imageService.getImages(buildRequest()).getImagesList().stream().map(
          IMAGE_CONVERTER).collect(
          Collectors.toSet());
    } catch (ResponseException e) {
      throw new IllegalStateException(
          String.format("Could not retrieve images due to error %s", e.getMessage()), e);
    }
  }

}
