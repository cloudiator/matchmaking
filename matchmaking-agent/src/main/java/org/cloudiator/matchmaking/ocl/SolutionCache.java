package org.cloudiator.matchmaking.ocl;

import com.google.inject.ImplementedBy;
import java.util.Optional;
import org.cloudiator.matchmaking.domain.Solution;

@ImplementedBy(SolutionCacheImpl.class)
public interface SolutionCache {

  void storeSolution(String userId, OclCsp oclCsp, Solution solution);

  Optional<Solution> retrieve(String userId, String id);

  Optional<Solution> retrieve(String userId, OclCsp oclCsp);

  void expire(String userId);

}
