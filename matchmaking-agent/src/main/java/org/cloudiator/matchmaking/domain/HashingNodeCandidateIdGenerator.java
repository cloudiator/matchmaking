package org.cloudiator.matchmaking.domain;

import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class HashingNodeCandidateIdGenerator implements NodeCandidateIdGenerator {

  private static final Funnel<NodeCandidate> NODE_CANDIDATE_FUNNEL = (Funnel<NodeCandidate>) (from, into) -> {
    into.putString(from.getCloud().getId(), Charsets.UTF_8);
    into.putString(from.getHardware().getId(), Charsets.UTF_8);
    into.putString(from.getLocation().getId(), Charsets.UTF_8);
    into.putString(from.getImage().getId(), Charsets.UTF_8);
  };
  private final static HashFunction HASH_FUNCTION = Hashing.md5();

  @Override
  public String generateId(NodeCandidate nodeCandidate) {
    return HASH_FUNCTION.hashObject(nodeCandidate, NODE_CANDIDATE_FUNNEL).toString();
  }
}
