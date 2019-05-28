package org.cloudiator.matchmaking.ocl;

import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import org.cloudiator.matchmaking.domain.NodeCandidate;

@Singleton
public class NodeCandidateCache implements Expirable {

  private static final Map<String, CachedNodeGenerator> CACHE = new ConcurrentHashMap<>();

  public synchronized static CachedNodeGenerator cache(String userId, NodeGenerator nodeGenerator) {
    return CACHE.computeIfAbsent(userId, s -> new CachedNodeGenerator(nodeGenerator.get()));
  }

  public static class CachedNodeGenerator implements NodeGenerator {

    private final NodeCandidates nodeCandidates;
    private final Map<String, NodeCandidate> nodeCandidateMap = new HashMap<>();

    private CachedNodeGenerator(NodeCandidates nodeCandidates) {
      this.nodeCandidates = nodeCandidates;
      nodeCandidates.stream().forEach(new Consumer<NodeCandidate>() {
        @Override
        public void accept(NodeCandidate nodeCandidate) {
          nodeCandidateMap.put(nodeCandidate.id(), nodeCandidate);
        }
      });
    }

    @Override
    public NodeCandidates get() {
      return nodeCandidates;
    }

    public NodeCandidate get(String id) {
      return nodeCandidateMap.get(id);
    }
  }

  @Override
  public synchronized void expire(String userId) {
    CACHE.remove(userId);
  }
}
