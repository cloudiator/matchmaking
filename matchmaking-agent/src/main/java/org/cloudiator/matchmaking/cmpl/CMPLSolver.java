package org.cloudiator.matchmaking.cmpl;

import cloudiator.CloudType;
import jCMPL.Cmpl;
import jCMPL.CmplException;
import jCMPL.CmplParameter;
import jCMPL.CmplSet;
import jCMPL.CmplSolElement;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.cloudiator.matchmaking.choco.ObjectMapper;
import org.cloudiator.matchmaking.choco.ObjectMapperImpl;
import org.cloudiator.matchmaking.domain.NodeCandidate;
import org.cloudiator.matchmaking.domain.Solution;
import org.cloudiator.matchmaking.domain.Solver;
import org.cloudiator.matchmaking.ocl.NodeCandidates;
import org.cloudiator.matchmaking.ocl.OclCsp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CMPLSolver implements Solver {

  private static final Logger LOGGER = LoggerFactory.getLogger(CMPLSolver.class);

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

    public Solution solveInternally(OclCsp oclCsp, Integer targetNodeSize,
        NodeCandidates nodeCandidates) {

      if (nodeCandidates.size() == 0) {
        return Solution.EMPTY_SOLUTION;
      }

      int totalSize = 0;
      int privateSize = 0;
      int publicSize = 0;

      for (String constraint : oclCsp.getUnparsedConstraints()) {

        if (constraint.contains("nodes->size() >=")) {
          totalSize = Integer.parseInt(constraint.replace("nodes->size() >=", "").trim());
        } else if (constraint
            .contains("nodes->select(n | n.cloud.type = CloudType::PUBLIC)->size() =")) {
          publicSize = Integer.parseInt(
              constraint
                  .replace("nodes->select(n | n.cloud.type = CloudType::PUBLIC)->size() =", "")
                  .replace("/ 2", "").trim());
        } else if (constraint
            .contains("nodes->select(n | n.cloud.type = CloudType::PRIVATE)->size() =")) {
          privateSize = Integer.parseInt(
              constraint
                  .replace("nodes->select(n | n.cloud.type = CloudType::PRIVATE)->size() =", "")
                  .replace("/ 2", "").trim());
        }
      }

      long start = System.currentTimeMillis();

      List<Integer> nodes = new LinkedList<>();
      //List<Integer> largerOrEqual4Cores = new LinkedList<>();
      //List<Integer> cores = new LinkedList<>();
      List<Double> prices = new LinkedList<>();
      List<Integer> privateCloud = new LinkedList<>();
      List<Integer> publicCloud = new LinkedList<>();
      //Set<Integer> locations = new LinkedHashSet<>();

      for (NodeCandidate nodeCandidate : nodeCandidates) {
        nodes.add(mapString("node.id", nodeCandidate.id()));
        //cores.add(nodeCandidate.getHardware().getCores());

        //locations.add(mapString("location.id", nodeCandidate.getLocation().getId()));

        prices.add(roundPrice(nodeCandidate.getPrice()));

        if (nodeCandidate.getCloud().getType().equals(CloudType.PRIVATE)) {
          privateCloud.add(1);
          publicCloud.add(0);
        } else {
          privateCloud.add(0);
          publicCloud.add(1);
        }

        System.out.println(new File(".").getAbsolutePath());

        //if (nodeCandidate.getHardware().getCores() >= 4) {
        //  largerOrEqual4Cores.add(1);
        //} else {
        //  largerOrEqual4Cores.add(0);
        //}
      }

//      int[][] inLocation = new int[locations.size()][nodes.size()];
//
//      int i = 0;
//      for (int location : locations) {
//        String locationId = mapStringBack("location.id", location);
//        int[] temp = new int[nodes.size()];
//        int j = 0;
//        for (int node : nodes) {
//          String nodeCandidateId = mapStringBack("node.id", node);
//          NodeCandidate nodeCandidate = nodeCandidates.getById(nodeCandidateId);
//          if (nodeCandidate.getLocation().getId().equals(locationId)) {
//            temp[j] = 1;
//          } else {
//            temp[j] = 0;
//          }
//          j++;
//        }
//        inLocation[i] = temp;
//        i++;
//      }

      try {
        CmplSet nodesSet = new CmplSet("NODES");
        nodesSet.setValues(nodes.toArray(new Integer[0]));

        //CmplSet locationsSet = new CmplSet("LOCATIONS");
        //locationsSet.setValues(locations.toArray(new Integer[0]));

        CmplParameter costParameter = new CmplParameter("costs", nodesSet);
        costParameter.setValues(prices.toArray(new Double[0]));

        CmplParameter privateParameter = new CmplParameter("private", nodesSet);
        privateParameter.setValues(privateCloud.toArray(new Integer[0]));

        CmplParameter publicParameter = new CmplParameter("public", nodesSet);
        publicParameter.setValues(publicCloud.toArray(new Integer[0]));

        //CmplParameter coreParameter = new CmplParameter("cores", nodesSet);
        //coreParameter.setValues(cores.toArray(new Integer[0]));

        //CmplParameter largerOrEqual4CoresParameters = new CmplParameter("largerOrEqual4Cores",
        //    nodesSet);
        //largerOrEqual4CoresParameters.setValues(largerOrEqual4Cores.toArray(new Integer[0]));

        //CmplParameter inLocationParameter = new CmplParameter("inLocation", locationsSet, nodesSet);
        //inLocationParameter.setValues(inLocation);

        //CmplParameter nodeSize = new CmplParameter("nodeSize");
        //nodeSize.setValues(targetNodeSize);

        CmplParameter nodeSize = new CmplParameter("nodeSize");
        nodeSize.setValues(totalSize);

        CmplParameter publicSizeParameter = new CmplParameter("publicSize");
        publicSizeParameter.setValues(publicSize / 2);

        CmplParameter privateSizeParameter = new CmplParameter("privateSize");
        privateSizeParameter.setValues(privateSize / 2);

        final File file = new File("both.cmpl");
        try (FileWriter fileWriter = new FileWriter(file);) {
          fileWriter.write(CMPLTemplate.BOTH);
        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
        Cmpl model = new Cmpl("both.cmpl");
        model.setSets(nodesSet);
        //model.setParameters(costParameter, coreParameter, largerOrEqual4CoresParameters, nodeSize,
        //    inLocationParameter);
        model.setParameters(costParameter, nodeSize, publicSizeParameter, privateSizeParameter,
            privateParameter, publicParameter);

        model.solve();

        long stop = System.currentTimeMillis();

        if (model.solverStatus() == Cmpl.SOLVER_OK) {

          LOGGER.debug(String.format("%s found solution: %s", this, model.solution()));

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
          of.setSolver(CMPLSolver.class);
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

    return new CMPLSolverInternal().solveInternally(oclCsp, targetNodeSize, nodeCandidates);
  }
}
