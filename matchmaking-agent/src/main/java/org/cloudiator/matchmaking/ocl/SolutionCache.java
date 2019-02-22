package org.cloudiator.matchmaking.ocl;

import com.google.inject.ImplementedBy;
import java.util.Optional;
import org.cloudiator.matchmaking.Expirable;
import org.cloudiator.matchmaking.domain.Solution;

@ImplementedBy(SolutionCacheImpl.class)
public interface SolutionCache extends Expirable {

  void storeSolution(String userId, OclCsp oclCsp, Solution solution);

  Optional<Solution> retrieve(String userId, String id);

  Optional<Solution> retrieve(String userId, OclCsp oclCsp);

}
