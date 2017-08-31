package org.cloudiator.ocl;

import cloudiator.Cloud;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import cloudiator.Image;
import cloudiator.OSArchitecture;
import cloudiator.OSFamily;
import cloudiator.OperatingSystem;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.cloudiator.messages.Image.ImageQueryRequest;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.ImageService;

public class ImageSupplier implements Supplier<Set<Image>> {

  private final CloudiatorFactory cloudiatorFactory = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
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
      return imageService.getImages(buildRequest()).getImagesList().stream().map(i -> {
        Image image = cloudiatorFactory.createImage();
        image.setId(i.getId());
        image.setName(i.getName());
        image.setProviderId(i.getProviderId());

        OperatingSystem os = cloudiatorFactory.createOperatingSystem();

        switch (i.getOperationSystem().getOperatingSystemArchitecture()) {
          case I386:
            os.setArchitecture(OSArchitecture.I368);
            break;
          case AMD64:
            os.setArchitecture(OSArchitecture.AMD64);
            break;
          case UNKOWN_OS_ARCH:
            os.setArchitecture(OSArchitecture.UNKOWN);
            break;
          case UNRECOGNIZED:
          default:
            throw new AssertionError(
                "Illegal architecture " + i.getOperationSystem().getOperatingSystemArchitecture());
        }

        switch (i.getOperationSystem().getOperatingSystemFamily()) {
          case UNKOWN_OS_FAMILY:
            os.setFamily(OSFamily.UNKNOWN);
            break;
          case UNRECOGNIZED:
            throw new AssertionError(
                "Illegal family " + i.getOperationSystem().getOperatingSystemFamily());
          default:
            os.setFamily(
                OSFamily.valueOf(i.getOperationSystem().getOperatingSystemFamily().name()));
        }
        os.setVersion(i.getOperationSystem().getOperatingSystemVersion());

        image.setOperatingSystem(os);

        return image;
      }).collect(
          Collectors.toSet());
    } catch (ResponseException e) {
      throw new IllegalStateException(
          String.format("Could not retrieve images due to error %s", e.getMessage()), e);
    }
  }
}
