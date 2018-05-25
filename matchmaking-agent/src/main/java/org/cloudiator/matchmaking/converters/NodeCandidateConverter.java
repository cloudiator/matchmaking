package org.cloudiator.matchmaking.converters;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;
import org.cloudiator.messages.entities.MatchmakingEntities;
import org.cloudiator.matchmaking.ocl.NodeCandidate;

public class NodeCandidateConverter implements
    OneWayConverter<NodeCandidate, MatchmakingEntities.NodeCandidate> {

  private static final ImageConverter IMAGE_CONVERTER = new ImageConverter();
  private static final HardwareConverter HARDWARE_CONVERTER = new HardwareConverter();
  private static final LocationConverter LOCATION_CONVERTER = new LocationConverter();
  private static final CloudConverter CLOUD_CONVERTER = new CloudConverter();

  @Nullable
  @Override
  public MatchmakingEntities.NodeCandidate apply(@Nullable NodeCandidate nodeCandidate) {
    return MatchmakingEntities.NodeCandidate.newBuilder()
        .setCloud(CLOUD_CONVERTER.apply(nodeCandidate.getCloud()))
        .setHardwareFlavor(HARDWARE_CONVERTER.applyBack(nodeCandidate.getHardware()))
        .setImage(IMAGE_CONVERTER.applyBack(nodeCandidate.getImage()))
        .setLocation(LOCATION_CONVERTER.applyBack(nodeCandidate.getLocation()))
        .setPrice(nodeCandidate.getPrice()).build();
  }
}
