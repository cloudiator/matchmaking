package org.cloudiator.matchmaking.domain;

import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HashingNodeCandidateIdGenerator implements NodeCandidateIdGenerator {

  private static final Funnel<NodeCandidate> IAAS_NODE_CANDIDATE_FUNNEL =
      (Funnel<NodeCandidate>) (from, into) -> {
        into.putString(from.getCloud().getId(), Charsets.UTF_8);
        into.putString(from.getHardware().getId(), Charsets.UTF_8);
        into.putString(from.getLocation().getId(), Charsets.UTF_8);
        into.putString(from.getImage().getId(), Charsets.UTF_8);
      };
  private static final Funnel<NodeCandidate> BYON_NODE_CANDIDATE_FUNNEL =
      IAAS_NODE_CANDIDATE_FUNNEL;
  private static final Funnel<NodeCandidate> FAAS_NODE_CANDIDATE_FUNNEL =
      (Funnel<NodeCandidate>) (from, into) -> {
        into.putString(from.getCloud().getId(), Charsets.UTF_8);
        into.putInt(from.getHardware().getRam());
        into.putString(from.getLocation().getId(), Charsets.UTF_8);
        into.putString(from.getEnvironment().getRuntime().getName(), Charsets.UTF_8);
        into.putDouble(from.getPricePerInvocation());
        into.putDouble(from.getMemoryPrice());
      };

  private final static HashFunction HASH_FUNCTION = Hashing.md5();

  @Override
  public String generateId(NodeCandidate nodeCandidate) {
    switch (nodeCandidate.getType()) {
      case IAAS:
        return HASH_FUNCTION.hashObject(nodeCandidate, IAAS_NODE_CANDIDATE_FUNNEL).toString();
      case FAAS:
        return HASH_FUNCTION.hashObject(nodeCandidate, FAAS_NODE_CANDIDATE_FUNNEL).toString();
      case BYON:
        return HASH_FUNCTION.hashObject(nodeCandidate, BYON_NODE_CANDIDATE_FUNNEL).toString();
      case PAAS:
      default:
        throw new IllegalStateException(
            "Unsupported node candidate type: " + nodeCandidate.getType());
    }
  }
}
