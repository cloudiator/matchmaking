package org.cloudiator.matchmaking.converters;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import cloudiator.Image;
import cloudiator.OSArchitecture;
import cloudiator.OSFamily;
import cloudiator.OperatingSystem;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import org.cloudiator.messages.entities.CommonEntities;
import org.cloudiator.messages.entities.CommonEntities.OperatingSystemArchitecture;
import org.cloudiator.messages.entities.CommonEntities.OperatingSystemFamily;
import org.cloudiator.messages.entities.CommonEntities.OperatingSystemVersion;
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

  private static final class OperatingSystemFamilyConverter implements
      TwoWayConverter<CommonEntities.OperatingSystemFamily, OSFamily> {

    @Override
    public OperatingSystemFamily applyBack(OSFamily osFamily) {
      switch (osFamily) {
        case UNKNOWN:
          return OperatingSystemFamily.UNKOWN_OS_FAMILY;
        default:
          return OperatingSystemFamily.valueOf(osFamily.getName());
      }

    }

    @Override
    public OSFamily apply(OperatingSystemFamily operatingSystemFamily) {
      switch (operatingSystemFamily) {
        case UNKOWN_OS_FAMILY:
          return OSFamily.UNKNOWN;
        case UNRECOGNIZED:
          throw new AssertionError(
              "Illegal OSFamily " + operatingSystemFamily);
        default:
          return OSFamily.valueOf(operatingSystemFamily.name());
      }
    }
  }

  private static final class OperatingSystemArchitectureConverter implements
      TwoWayConverter<CommonEntities.OperatingSystemArchitecture, OSArchitecture> {

    @Override
    public CommonEntities.OperatingSystemArchitecture applyBack(OSArchitecture osArchitecture) {
      switch (osArchitecture) {
        case AMD64:
          return OperatingSystemArchitecture.AMD64;
        case I368:
          return OperatingSystemArchitecture.I386;
        case ARM:
          return OperatingSystemArchitecture.ARM;
        case UNKOWN:
          return OperatingSystemArchitecture.UNKOWN_OS_ARCH;
        default:
          throw new AssertionError(
              String.format("OSArchitecture %s is unknown", osArchitecture));
      }
    }

    @Override
    public OSArchitecture apply(
        CommonEntities.OperatingSystemArchitecture operatingSystemArchitecture) {
      switch (operatingSystemArchitecture) {
        case I386:
          return OSArchitecture.I368;
        case AMD64:
          return OSArchitecture.AMD64;
        case ARM:
          return OSArchitecture.ARM;
        case UNKOWN_OS_ARCH:
          return OSArchitecture.UNKOWN;
        case UNRECOGNIZED:
        default:
          throw new AssertionError(
              "Illegal architecture " + operatingSystemArchitecture);
      }
    }
  }

  private static final class OperatingSystemConverter implements
      TwoWayConverter<CommonEntities.OperatingSystem, OperatingSystem> {

    private static final OperatingSystemFamilyConverter OS_FAMILY_CONVERTER = new OperatingSystemFamilyConverter();
    private static final OperatingSystemArchitectureConverter OS_ARCH_CONVERTER = new OperatingSystemArchitectureConverter();

    @Override
    public CommonEntities.OperatingSystem applyBack(OperatingSystem operatingSystem) {
      final CommonEntities.OperatingSystem.Builder builder = CommonEntities.OperatingSystem
          .newBuilder().setOperatingSystemArchitecture(
              OS_ARCH_CONVERTER.applyBack(operatingSystem.getArchitecture()))
          .setOperatingSystemFamily(OS_FAMILY_CONVERTER.applyBack(operatingSystem.getFamily()));

      if (operatingSystem.getVersion() != null) {
        builder.setOperatingSystemVersion(
            OperatingSystemVersion.newBuilder().setVersion(operatingSystem.getVersion()).build());
      }
      return builder.build();
    }

    @Override
    public OperatingSystem apply(CommonEntities.OperatingSystem operatingSystem) {
      OperatingSystem os = CLOUDIATOR_FACTORY.createOperatingSystem();
      os.setFamily(OS_FAMILY_CONVERTER.apply(operatingSystem.getOperatingSystemFamily()));

      if (!operatingSystem.hasOperatingSystemVersion()) {
        os.setVersion(null);
      } else {
        os.setVersion(operatingSystem.getOperatingSystemVersion().getVersion());
      }
      os.setArchitecture(OS_ARCH_CONVERTER.apply(operatingSystem.getOperatingSystemArchitecture()));
      return os;

    }
  }
}
