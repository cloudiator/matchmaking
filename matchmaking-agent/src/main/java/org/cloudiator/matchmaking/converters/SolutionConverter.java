package org.cloudiator.matchmaking.converters;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.messages.entities.MatchmakingEntities;
import org.cloudiator.messages.entities.MatchmakingEntities.Solution.Builder;

public class SolutionConverter implements OneWayConverter<Solution, MatchmakingEntities.Solution> {

  public static final SolutionConverter INSTANCE = new SolutionConverter();

  private static final NodeCandidateConverter NODE_CANDIDATE_CONVERTER = NodeCandidateConverter.INSTANCE;

  private SolutionConverter() {
  }

  @Nullable
  @Override
  public MatchmakingEntities.Solution apply(@Nullable Solution solution) {

    if (solution == null) {
      return null;
    }

    final Builder builder = MatchmakingEntities.Solution.newBuilder().setId(solution.getId())
        .setIsOptimal(solution.isOptimal())
        .setCosts(solution.getCosts()).setTime(solution.getTime().orElse(0F));

    solution.getList().forEach(
        nodeCandidate -> builder.addNodeCandidates(NODE_CANDIDATE_CONVERTER.apply(nodeCandidate)));

    return builder.build();
  }
}
