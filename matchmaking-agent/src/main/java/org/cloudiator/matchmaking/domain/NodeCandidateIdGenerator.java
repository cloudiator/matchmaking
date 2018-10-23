package org.cloudiator.matchmaking.domain;

public interface NodeCandidateIdGenerator {

  String generateId(NodeCandidate nodeCandidate);

}
