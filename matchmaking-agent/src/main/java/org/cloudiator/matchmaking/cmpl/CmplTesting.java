package org.cloudiator.matchmaking.cmpl;

import jCMPL.Cmpl;
import jCMPL.CmplException;
import jCMPL.CmplParameter;
import jCMPL.CmplSet;
import jCMPL.CmplSolElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.cloudiator.matchmaking.choco.ObjectMapper;
import org.cloudiator.matchmaking.choco.ObjectMapperImpl;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.ocl.NodeCandidates;

public class CmplTesting {

  private static final Map<String, ObjectMapper> objectMappers = new HashMap<>();

  private static Integer mapString(String name, String string) {
    objectMappers.putIfAbsent(name, new ObjectMapperImpl());
    //noinspection unchecked
    return objectMappers.get(name).applyAsInt(string);
  }

  private static String mapStringBack(String name, int i) {
    return (String) objectMappers.get(name).applyBack(i);
  }


  public static Solution solve(int targetNodeSize, NodeCandidates nodeCandidates) {

    long start = System.currentTimeMillis();

    List<Integer> nodes = new LinkedList<>();
    List<Integer> largerOrEqual4Cores = new LinkedList<>();
    List<Integer> cores = new LinkedList<>();
    List<Double> prices = new LinkedList<>();

    for (NodeCandidate nodeCandidate : nodeCandidates) {
      nodes.add(mapString("node.id", nodeCandidate.id()));
      cores.add(nodeCandidate.getHardware().getCores());
      prices.add(nodeCandidate.getPrice());
      if (nodeCandidate.getHardware().getCores() >= 4) {
        largerOrEqual4Cores.add(1);
      } else {
        largerOrEqual4Cores.add(0);
      }
    }

    try {
      CmplSet nodesSet = new CmplSet("NODES");
      nodesSet.setValues(nodes.toArray(new Integer[0]));

      CmplParameter costParameter = new CmplParameter("costs", nodesSet);
      costParameter.setValues(prices.toArray(new Double[0]));

      CmplParameter coreParameter = new CmplParameter("cores", nodesSet);
      coreParameter.setValues(cores.toArray(new Integer[0]));

      CmplParameter largerOrEqual4CoresParameters = new CmplParameter("largerOrEqual4Cores",
          nodesSet);
      largerOrEqual4CoresParameters.setValues(largerOrEqual4Cores.toArray(new Integer[0]));

      CmplParameter nodeSize = new CmplParameter("nodeSize");
      nodeSize.setValues(targetNodeSize);

      Cmpl model = new Cmpl("nodes.cmpl");
      model.setSets(nodesSet);
      model.setParameters(costParameter, coreParameter, largerOrEqual4CoresParameters, nodeSize);

      model.solve();

      long stop = System.currentTimeMillis();

      if (model.solverStatus() == Cmpl.SOLVER_OK) {
        List<NodeCandidate> solutionCandidates = new ArrayList<>();
        for (CmplSolElement v : model.solution().variables()) {
          if ((long) v.activity() != 0) {
            final String s = mapStringBack("node.id", v.idx() + 1);
            final NodeCandidate byId = nodeCandidates.getById(s);
            for (int i = 0; i < (long) v.activity(); i++) {
              solutionCandidates.add(byId);
            }
          }
        }
        final Solution of = Solution.of(solutionCandidates);
        of.setIsOptimal(true);
        of.setTime(stop - start);
        return of;
      }
    } catch (CmplException e) {
      e.printStackTrace();
      return Solution.EMPTY_SOLUTION;
    }

    return Solution.EMPTY_SOLUTION;
  }


}
