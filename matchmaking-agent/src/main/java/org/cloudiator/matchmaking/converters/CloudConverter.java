package org.cloudiator.matchmaking.converters;

import cloudiator.Api;
import cloudiator.Cloud;
import cloudiator.CloudConfiguration;
import cloudiator.CloudCredential;
import cloudiator.CloudState;
import cloudiator.CloudType;
import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import cloudiator.Property;
import com.google.common.base.Strings;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import java.util.stream.Collectors;
import org.cloudiator.matchmaking.ocl.ByonCloudUtil;
import org.cloudiator.messages.entities.IaasEntities;
import org.cloudiator.messages.entities.IaasEntities.Cloud.Builder;
import org.cloudiator.messages.entities.IaasEntities.Configuration;
import org.cloudiator.messages.entities.IaasEntities.Credential;

public class CloudConverter implements TwoWayConverter<Cloud, IaasEntities.Cloud> {

  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private static final CloudTypeConverter TYPE_CONVERTER = new CloudTypeConverter();
  private static final ApiConverter API_CONVERTER = new ApiConverter();
  private static final CloudCredentialConverter CLOUD_CREDENTIAL_CONVERTER = new CloudCredentialConverter();
  private static final CloudConfigurationConverter CLOUD_CONFIGURATION_CONVERTER = new CloudConfigurationConverter();
  private static final CloudStateConverter CLOUD_STATE_CONVERTER = new CloudStateConverter();

  @Override
  public Cloud applyBack(IaasEntities.Cloud cloud) {
    final boolean checkByon = ByonCloudUtil.isByon(cloud.getId());

    if (checkByon) {
      throw new IllegalStateException("Cannot process Cloud contents corresponding to byon");
    }

    Cloud modelCloud = CLOUDIATOR_FACTORY.createCloud();
    modelCloud.setId(cloud.getId());
    modelCloud.setEndpoint(cloud.getEndpoint());
    modelCloud.setType(TYPE_CONVERTER.applyBack(cloud.getCloudType()));
    modelCloud.setApi(API_CONVERTER.applyBack(cloud.getApi()));
    modelCloud
        .setConfiguration(CLOUD_CONFIGURATION_CONVERTER.applyBack(cloud.getConfiguration()));
    modelCloud.setState(CLOUD_STATE_CONVERTER.applyBack(cloud.getState()));
    modelCloud.setDiagnostic(cloud.getDiagnostic());
    modelCloud.setCloudcredential(CLOUD_CREDENTIAL_CONVERTER.applyBack(cloud.getCredential()));
    modelCloud.setOwner(cloud.getUserId());
    return modelCloud;
  }

  @Override
  public IaasEntities.Cloud apply(Cloud cloud) {
    if (cloud == null) {
      return null;
    }

    final Builder builder = IaasEntities.Cloud.newBuilder().setId(cloud.getId())
        .setApi(API_CONVERTER.apply(cloud.getApi()))
        .setCredential(CLOUD_CREDENTIAL_CONVERTER.apply(cloud.getCloudcredential()))
        .setUserId(cloud.getOwner());

    final boolean checkByon = ByonCloudUtil.isByon(cloud.getId());

    if (!checkByon) {
      builder.setEndpoint(cloud.getEndpoint())
          .setCloudType(TYPE_CONVERTER.apply(cloud.getType()))
          .setState(CLOUD_STATE_CONVERTER.apply(cloud.getState()));
    }

    if (!Strings.isNullOrEmpty(cloud.getDiagnostic())) {
      builder.setDiagnostic(cloud.getDiagnostic());
    }

    return builder.build();
  }

  private static class ApiConverter implements TwoWayConverter<Api, IaasEntities.Api> {

    @Override
    public Api applyBack(IaasEntities.Api api) {
      final Api modelApi = CLOUDIATOR_FACTORY.createApi();
      modelApi.setProviderName(api.getProviderName());
      return modelApi;
    }

    @Override
    public IaasEntities.Api apply(Api api) {
      return IaasEntities.Api.newBuilder().setProviderName(api.getProviderName()).build();
    }
  }

  private static class CloudCredentialConverter implements
      TwoWayConverter<CloudCredential, IaasEntities.Credential> {

    @Override
    public CloudCredential applyBack(Credential credential) {
      final CloudCredential cloudCredential = CLOUDIATOR_FACTORY.createCloudCredential();
      cloudCredential.setSecret(credential.getSecret());
      cloudCredential.setUser(credential.getUser());
      return cloudCredential;
    }

    @Override
    public Credential apply(CloudCredential cloudCredential) {
      return IaasEntities.Credential.newBuilder().setUser(cloudCredential.getUser())
          .setSecret(cloudCredential.getUser()).build();
    }
  }

  private static class CloudStateConverter implements
      TwoWayConverter<CloudState, IaasEntities.CloudState> {

    @Override
    public CloudState applyBack(IaasEntities.CloudState cloudState) {
      switch (cloudState) {
        case CLOUD_STATE_OK:
          return CloudState.OK;
        case CLOUD_STATE_ERROR:
          return CloudState.ERROR;
        case CLOUD_STATE_DELETED:
        case CLOUD_STATE_NEW:
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Illegal or unknown cloudState " + cloudState);
      }
    }

    @Override
    public IaasEntities.CloudState apply(CloudState cloudState) {
      switch (cloudState) {
        case OK:
          return IaasEntities.CloudState.CLOUD_STATE_OK;
        case ERROR:
          return IaasEntities.CloudState.CLOUD_STATE_ERROR;
        default:
          throw new AssertionError("Illegal or unknown cloudState " + cloudState);
      }
    }
  }

  private static class CloudConfigurationConverter implements
      TwoWayConverter<CloudConfiguration, IaasEntities.Configuration> {

    @Override
    public CloudConfiguration applyBack(Configuration configuration) {

      final CloudConfiguration cloudConfiguration = CLOUDIATOR_FACTORY.createCloudConfiguration();
      cloudConfiguration.setNodeGroup(configuration.getNodeGroup());
      cloudConfiguration.getProperties()
          .addAll(configuration.getPropertiesMap().entrySet().stream().map(
              entry -> {
                final Property modelProperty = CLOUDIATOR_FACTORY.createProperty();
                modelProperty.setKey(entry.getKey());
                modelProperty.setValue(entry.getValue());
                return modelProperty;
              }).collect(Collectors.toList()));

      return cloudConfiguration;
    }

    @Override
    public Configuration apply(CloudConfiguration cloudConfiguration) {
      return Configuration.newBuilder().setNodeGroup(cloudConfiguration.getNodeGroup())
          .putAllProperties(cloudConfiguration.getProperties().stream()
              .collect(Collectors.toMap(Property::getKey, Property::getValue)))
          .build();
    }
  }


  private static class CloudTypeConverter implements
      TwoWayConverter<CloudType, IaasEntities.CloudType> {

    @Override
    public CloudType applyBack(IaasEntities.CloudType cloudType) {
      switch (cloudType) {
        case PRIVATE_CLOUD:
          return CloudType.PRIVATE;
        case PUBLIC_CLOUD:
          return CloudType.PUBLIC;
        case SIMULATION_CLOUD:
          return CloudType.SIMULATION;
        case UNRECOGNIZED:
        default:
          throw new AssertionError(
              String.format("CloudType %s is either unrecognized or illegal", cloudType));
      }
    }

    @Override
    public IaasEntities.CloudType apply(CloudType cloudType) {
      switch (cloudType) {
        case PUBLIC:
          return IaasEntities.CloudType.PUBLIC_CLOUD;
        case PRIVATE:
          return IaasEntities.CloudType.PRIVATE_CLOUD;
        case SIMULATION:
          return IaasEntities.CloudType.SIMULATION_CLOUD;
        default:
          throw new AssertionError(String.format("CloudType %s is not known.", cloudType));
      }
    }
  }

}
