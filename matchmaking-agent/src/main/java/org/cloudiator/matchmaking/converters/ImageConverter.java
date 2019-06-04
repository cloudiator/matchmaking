package org.cloudiator.matchmaking.converters;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import cloudiator.Image;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Image.Builder;

public class ImageConverter implements TwoWayConverter<IaasEntities.Image, Image> {

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private static final LocationConverter LOCATION_CONVERTER = new LocationConverter();
  private static final OperatingSystemConverter OS_CONVERTER = new OperatingSystemConverter();
  private static final DiscoveryItemStateConverter DISCOVERY_ITEM_STATE_CONVERTER = DiscoveryItemStateConverter.INSTANCE;

  @Override
  public IaasEntities.Image applyBack(Image image) {
    if (image == null) {
      return null;
    }

    final Builder builder = IaasEntities.Image.newBuilder().setId(image.getId())
        .setProviderId(image.getProviderId())
        .setOperationSystem(OS_CONVERTER.applyBack(image.getOperatingSystem()))
        .setName(image.getName())
        .setState(DISCOVERY_ITEM_STATE_CONVERTER.apply(image.getState()))
        .setUserId(image.getOwner());

    if (image.getLocation() != null) {
      builder.setLocation(LOCATION_CONVERTER.applyBack(image.getLocation()));
    }

    return builder.build();

  }

  @Override
  public Image apply(IaasEntities.Image image) {
    if (image == null) {
      return null;
    }

    Image imageModel = CLOUDIATOR_FACTORY.createImage();
    imageModel.setId(image.getId());
    imageModel.setName(image.getName());
    imageModel.setProviderId(image.getProviderId());
    imageModel.setState(DISCOVERY_ITEM_STATE_CONVERTER.applyBack(image.getState()));
    imageModel.setOwner(image.getUserId());

    if (image.hasLocation()) {
      imageModel.setLocation(LOCATION_CONVERTER.apply(image.getLocation()));
    }
    imageModel.setOperatingSystem(OS_CONVERTER.apply(image.getOperationSystem()));
    return imageModel;
  }
}
