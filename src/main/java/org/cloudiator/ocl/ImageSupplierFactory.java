package org.cloudiator.ocl;

import cloudiator.Cloud;
import com.google.inject.Inject;
import org.cloudiator.messaging.services.ImageService;

public class ImageSupplierFactory {

  private final ImageService imageService;

  @Inject public ImageSupplierFactory(ImageService imageService) {
    this.imageService = imageService;
  }

  public ImageSupplier newInstance(Cloud cloud, String userId) {
    return new ImageSupplier(imageService, userId, cloud);
  }

}
