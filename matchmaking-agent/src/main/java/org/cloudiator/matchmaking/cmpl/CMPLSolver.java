package org.cloudiator.matchmaking.cmpl;

import jCMPL.Cmpl;
import jCMPL.CmplException;
import jCMPL.CmplParameter;
import jCMPL.CmplSet;
import jCMPL.CmplSolElement;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.choco.ObjectMapper;
import org.cloudiator.matchmaking.choco.ObjectMapperImpl;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.domain.Solver;
import org.cloudiator.matchmaking.ocl.NodeCandidates;
import org.cloudiator.matchmaking.ocl.OclCsp;

public class CMPLSolver implements Solver {

  private static class CMPLSolverInternal {
    private final Map<String, ObjectMapper> objectMappers = new HashMap<>();

    private Integer mapString(String name, String string) {
      objectMappers.putIfAbsent(name, new ObjectMapperImpl());
      //noinspection unchecked
      return objectMappers.get(name).applyAsInt(string);
    }

    private String mapStringBack(String name, int i) {
      return (String) objectMappers.get(name).applyBack(i);
    }

    private static Double roundPrice(Double price) {
      DecimalFormat df = new DecimalFormat("#.####");
      df.setRoundingMode(RoundingMode.CEILING);
      return Double.valueOf(df.format(price));
    }

    public Solution solveInternally(int targetNodeSize, NodeCandidates nodeCandidates) {

      if (nodeCandidates.size() == 0) {
        return Solution.EMPTY_SOLUTION;
      }

      long start = System.currentTimeMillis();

      List<Integer> nodes = new LinkedList<>();
      List<Integer> largerOrEqual4Cores = new LinkedList<>();
      List<Integer> cores = new LinkedList<>();
      List<Double> prices = new LinkedList<>();
      Set<Integer> locations = new LinkedHashSet<>();

      for (NodeCandidate nodeCandidate : nodeCandidates) {
        nodes.add(mapString("node.id", nodeCandidate.id()));
        cores.add(nodeCandidate.getHardware().getCores());

        locations.add(mapString("location.id", nodeCandidate.getLocation().getId()));

        prices.add(roundPrice(nodeCandidate.getPrice()));
        if (nodeCandidate.getHardware().getCores() >= 4) {
          largerOrEqual4Cores.add(1);
        } else {
          largerOrEqual4Cores.add(0);
        }
      }

      int[][] inLocation = new int[locations.size()][nodes.size()];

      int i = 0;
      for(int location : locations) {
        String locationId = mapStringBack("location.id", location);
        int[] temp = new int[nodes.size()];
        int j = 0;
        for(int node : nodes) {
          String nodeCandidateId = mapStringBack("node.id", node);
          NodeCandidate nodeCandidate = nodeCandidates.getById(nodeCandidateId);
          if (nodeCandidate.getLocation().getId().equals(locationId)) {
            temp[j] = 1;
          } else {
            temp[j] = 0;
          }
          j++;
        }
        inLocation[i] = temp;
        i++;
      }

      try {
        CmplSet nodesSet = new CmplSet("NODES");
        nodesSet.setValues(nodes.toArray(new Integer[0]));

        CmplSet locationsSet = new CmplSet("LOCATIONS");
        locationsSet.setValues(locations.toArray(new Integer[0]));

        CmplParameter costParameter = new CmplParameter("costs", nodesSet);
        costParameter.setValues(prices.toArray(new Double[0]));

        CmplParameter coreParameter = new CmplParameter("cores", nodesSet);
        coreParameter.setValues(cores.toArray(new Integer[0]));

        CmplParameter largerOrEqual4CoresParameters = new CmplParameter("largerOrEqual4Cores",
            nodesSet);
        largerOrEqual4CoresParameters.setValues(largerOrEqual4Cores.toArray(new Integer[0]));

        CmplParameter inLocationParameter = new CmplParameter("inLocation", locationsSet, nodesSet);
        inLocationParameter.setValues(inLocation);

        CmplParameter nodeSize = new CmplParameter("nodeSize");
        nodeSize.setValues(targetNodeSize);

        Cmpl model = new Cmpl("nodes.cmpl");
        model.setSets(nodesSet, locationsSet);
        model.setParameters(costParameter, coreParameter, largerOrEqual4CoresParameters, nodeSize,
            inLocationParameter);

        model.solve();

        long stop = System.currentTimeMillis();

        if (model.solverStatus() == Cmpl.SOLVER_OK) {
          List<NodeCandidate> solutionCandidates = new ArrayList<>();
          for (CmplSolElement v : model.solution().variables()) {
            if ((long) v.activity() != 0) {
              final String s = mapStringBack("node.id", v.idx() + 1);
              final NodeCandidate byId = nodeCandidates.getById(s);

              if (byId == null) {
                throw new IllegalStateException("Could not find node candidate with id " + s);
              }

              for (int h = 0; h < (long) v.activity(); h++) {
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

  @Override
  public Solution solve(OclCsp oclCsp, NodeCandidates nodeCandidates,
      @Nullable Solution existingSolution, @Nullable Integer targetNodeSize) {

    return new CMPLSolverInternal().solveInternally(targetNodeSize, nodeCandidates);
  }
}
