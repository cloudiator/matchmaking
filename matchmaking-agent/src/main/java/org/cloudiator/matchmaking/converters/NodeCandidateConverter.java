package org.cloudiator.matchmaking.converters;

import cloudiator.Environment;
import cloudiator.NodeType;
import cloudiator.Runtime;
import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.messages.entities.MatchmakingEntities;

public class NodeCandidateConverter implements
    OneWayConverter<NodeCandidate, MatchmakingEntities.NodeCandidate> {

  private static final ImageConverter IMAGE_CONVERTER = new ImageConverter();
  private static final HardwareConverter HARDWARE_CONVERTER = new HardwareConverter();
  private static final LocationConverter LOCATION_CONVERTER = new LocationConverter();
  private static final CloudConverter CLOUD_CONVERTER = new CloudConverter();

  public static final NodeCandidateConverter INSTANCE = new NodeCandidateConverter();

  private NodeCandidateConverter() {
  }

  @Nullable
  @Override
  public MatchmakingEntities.NodeCandidate apply(@Nullable NodeCandidate nodeCandidate) {
    switch (nodeCandidate.getType()) {
      case IAAS:
      case SIMULATION:
        return applyIaas(nodeCandidate);
      case FAAS:
        return applyFaas(nodeCandidate);
      case BYON:
        return applyByon(nodeCandidate);
      case PAAS:
      default:
        throw new IllegalStateException(
            "Unsupported node candidate type: " + nodeCandidate.getType());
    }
  }

  private MatchmakingEntities.NodeCandidate applyIaas(NodeCandidate nodeCandidate) {
    return MatchmakingEntities.NodeCandidate.newBuilder()
        .setId(nodeCandidate.id())
        .setType(convertType(nodeCandidate.getType()))
        .setCloud(CLOUD_CONVERTER.apply(nodeCandidate.getCloud()))
        .setHardwareFlavor(HARDWARE_CONVERTER.applyBack(nodeCandidate.getHardware()))
        .setImage(IMAGE_CONVERTER.applyBack(nodeCandidate.getImage()))
        .setLocation(LOCATION_CONVERTER.applyBack(nodeCandidate.getLocation()))
        .setPrice(nodeCandidate.getPrice())
        .build();
  }

  private MatchmakingEntities.NodeCandidate applyByon(NodeCandidate nodeCandidate) {
    return MatchmakingEntities.NodeCandidate.newBuilder()
        .setId(nodeCandidate.id())
        .setType(convertType(nodeCandidate.getType()))
        .setCloud(CLOUD_CONVERTER.apply(nodeCandidate.getCloud()))
        .setHardwareFlavor(HARDWARE_CONVERTER.applyBack(nodeCandidate.getHardware()))
        .setImage(IMAGE_CONVERTER.applyBack(nodeCandidate.getImage()))
        .setLocation(LOCATION_CONVERTER.applyBack(nodeCandidate.getLocation()))
        .build();
  }

  private MatchmakingEntities.NodeCandidate applyFaas(NodeCandidate nodeCandidate) {
    return MatchmakingEntities.NodeCandidate.newBuilder()
        .setId(nodeCandidate.id())
        .setType(convertType(nodeCandidate.getType()))
        .setCloud(CLOUD_CONVERTER.apply(nodeCandidate.getCloud()))
        .setLocation(LOCATION_CONVERTER.applyBack(nodeCandidate.getLocation()))
        .setHardwareFlavor(HARDWARE_CONVERTER.applyBack(nodeCandidate.getHardware()))
        .setPricePerInvocation(nodeCandidate.getPricePerInvocation())
        .setMemoryPrice(nodeCandidate.getMemoryPrice())
        .setEnvironment(convertEnvironment(nodeCandidate.getEnvironment()))
        .build();
  }

  private MatchmakingEntities.NodeCandidateType convertType(NodeType type) {
    switch (type) {
      case IAAS:
        return MatchmakingEntities.NodeCandidateType.NC_IAAS;
      case FAAS:
        return MatchmakingEntities.NodeCandidateType.NC_FAAS;
      case PAAS:
        return MatchmakingEntities.NodeCandidateType.NC_PAAS;
      case BYON:
        return MatchmakingEntities.NodeCandidateType.NC_BYON;
      case SIMULATION:
        return MatchmakingEntities.NodeCandidateType.NC_SIMULATION;
      default:
        throw new IllegalStateException();

    }
  }

  private MatchmakingEntities.Environment convertEnvironment(Environment environment) {
    return MatchmakingEntities.Environment.newBuilder()
        .setRuntime(convertRuntime(environment.getRuntime())).build();
  }

  private MatchmakingEntities.Runtime convertRuntime(Runtime runtime) {
    switch (runtime) {
      case NODEJS:
        return MatchmakingEntities.Runtime.NODEJS;
      case PYTHON:
        return MatchmakingEntities.Runtime.PYTHON;
      case JAVA:
        return MatchmakingEntities.Runtime.JAVA;
      case DOTNET:
        return MatchmakingEntities.Runtime.DOTNET;
      case GO:
        return MatchmakingEntities.Runtime.GO;
      default:
        throw new IllegalStateException("FaasRuntime type not known " + runtime.toString());
    }
  }
}
