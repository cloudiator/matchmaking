package choco;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import cloudiator.CloudiatorFactory;
import cloudiator.CloudiatorPackage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;
import org.cloudiator.ocl.NodeCandidate;
import org.cloudiator.ocl.Solution;

public class ChocoSolver {


  private static final CloudiatorFactory CLOUDIATOR_FACTORY = CloudiatorPackage.eINSTANCE
      .getCloudiatorFactory();
  private final Set<NodeCandidate> possibleNodes;


  public ChocoSolver(Set<NodeCandidate> possibleNodes) {
    this.possibleNodes = possibleNodes;
  }

  public Solution solveDirect(int numberOfNodes, TimeLimit timeLimit) {
    return solve(numberOfNodes, timeLimit, Solution.EMPTY_SOLUTION);
  }

  public Solution solveIteratively(int numberOfNodes, TimeLimit timeLimit) {
    float time = 0;
    Solution solution = Solution.EMPTY_SOLUTION;
    for (int i = 2; i <= numberOfNodes; i++) {
      solution = solve(i, timeLimit, solution);
      time = time + solution.getTime();
    }
    solution.setTime(time);
    return solution;
  }

  private Solution solve(int numberOfNodes, TimeLimit timeLimit, Solution existingSolution) {

    checkArgument(numberOfNodes > existingSolution.nodeSize());

    int existingNodes = existingSolution.nodeSize();

    Model model = new Model("Selection CSP");

    Set<NodeCandidate> nodesToConsider = this.possibleNodes.stream()
        .filter(nc -> !nc.getPrice().equals(Double.MAX_VALUE)).collect(
            Collectors.toSet());

    System.out
        .println(String.format("%s nodes with valid pricing information", nodesToConsider.size()));

    //calculate average prices

    //sums
    Map<String, Double> hardwarePrices = new HashMap<>();
    Map<String, Double> locationPrices = new HashMap<>();
    Map<String, Double> countryPrices = new HashMap<>();
    Map<String, Double> imagePrices = new HashMap<>();

    //counts
    Map<String, Integer> hardwareCounts = new HashMap<>();
    Map<String, Integer> locationCounts = new HashMap<>();
    Map<String, Integer> countryCounts = new HashMap<>();
    Map<String, Integer> imageCounts = new HashMap<>();

    //averages
    Map<String, Double> hardwareAverage = new HashMap<>();
    Map<String, Double> locationAverage = new HashMap<>();
    Map<String, Double> countryAverage = new HashMap<>();
    Map<String, Double> imageAverage = new HashMap<>();

    for (NodeCandidate nc : nodesToConsider) {
      hardwarePrices.compute(nc.getHardware().getId(),
          (k, v) -> v == null ? nc.getPrice() : v + nc.getPrice());
      hardwareCounts.compute(nc.getHardware().getId(),
          (k, v) -> v == null ? 1 : v + 1);

      locationPrices.compute(nc.getLocation().getId(),
          (k, v) -> v == null ? nc.getPrice() : v + nc.getPrice());
      locationCounts.compute(nc.getLocation().getId(),
          (k, v) -> v == null ? 1 : v + 1);

      countryPrices.compute(nc.getLocation().getCountry(),
          (k, v) -> v == null ? nc.getPrice() : v + nc.getPrice());
      countryCounts.compute(nc.getLocation().getCountry(),
          (k, v) -> v == null ? 1 : v + 1);

      imagePrices.compute(nc.getImage().getId(),
          (k, v) -> v == null ? nc.getPrice() : v + nc.getPrice());
      imageCounts.compute(nc.getImage().getId(),
          (k, v) -> v == null ? 1 : v + 1);
    }

    hardwarePrices.forEach((s, aDouble) -> hardwareAverage.put(s, aDouble / hardwareCounts.get(s)));
    locationPrices.forEach((s, aDouble) -> locationAverage.put(s, aDouble / locationCounts.get(s)));
    countryPrices.forEach((s, aDouble) -> countryAverage.put(s, aDouble / countryCounts.get(s)));
    imagePrices.forEach((s, aDouble) -> imageAverage.put(s, aDouble / imageCounts.get(s)));

    Set<Integer> coreDomain = new HashSet<>();
    Set<Integer> ramDomain = new HashSet<>();
    Set<Integer> countryDomain = new HashSet<>();
    Set<Integer> priceDomain = new HashSet<>();
    Set<Integer> imageOidDomain = new HashSet<>();
    Set<Integer> hardwareOidDomain = new HashSet<>();
    Set<Integer> locationOidDomain = new HashSet<>();

    IntVar[] coreVariables = new IntVar[numberOfNodes];
    IntVar[] ramVariables = new IntVar[numberOfNodes];
    IntVar[] countryVariables = new IntVar[numberOfNodes];
    IntVar[] hardwareOidVariables = new IntVar[numberOfNodes];
    IntVar[] locationOidVariables = new IntVar[numberOfNodes];
    IntVar[] imageOidVariables = new IntVar[numberOfNodes];
    //IntVar[] cloudOidVariables = new IntVar[numberOfNodes];
    IntVar[] priceVariables = new IntVar[numberOfNodes];

    ObjectMapper<String> hardwareMapper = new SortedObjectMapper<>(hardwareAverage.keySet(),
        Comparator.comparing(hardwareAverage::get));
    ObjectMapper<String> countryMapper = new SortedObjectMapper<>(countryAverage.keySet(),
        Comparator.comparing(countryAverage::get));
    ObjectMapper<String> locationMapper = new SortedObjectMapper<>(locationAverage.keySet(),
        Comparator.comparing(locationAverage::get));
    //ObjectMapper<String> cloudMapper = new ObjectMapperImpl<>();
    ObjectMapper<String> imageMapper = new SortedObjectMapper<>(imageAverage.keySet(),
        Comparator.comparing(imageAverage::get));
    ObjectMapper<Double> priceMapper = new DoubleMapper();

    //cores to oid map
    Map<Integer, Set<Integer>> coresToOid = new HashMap<>();
    //ram to oid map
    Map<Integer, Set<Integer>> ramToOid = new HashMap<>();
    //all oid constraints
    //Map<Integer, Integer> hardwareOidsToCores = new HashMap<>();
    //Map<Integer, Integer> hardwareOidsToRam = new HashMap<>();
    //country to oid mao
    Map<Integer, Set<Integer>> countryToOid = new HashMap<>();
    //oid to country
    //Map<Integer, Integer> locationOidsToCountry = new HashMap<>();

    //relationship between location and cloud
    //Map<Integer, Set<Integer>> locationOidsToCloudOids = new HashMap<>();
    //Map<Integer, Set<Integer>> cloudOidsToLocationOids = new HashMap<>();

    //relationship between hardware and cloud
    //Map<Integer, Set<Integer>> hardwareOidsToCloudOids = new HashMap<>();
    //Map<Integer, Set<Integer>> cloudOidsToHardwareOids = new HashMap<>();

    //relationship between image and cloud
    //Map<Integer, Set<Integer>> imageOidsToCloudOids = new HashMap<>();
    //Map<Integer, Set<Integer>> cloudOidsToImageOids = new HashMap<>();

    //relationship between image and location
    //Map<Integer, Set<Integer>> imageOidsToLocationOids = new HashMap<>();
    Map<Integer, Set<Integer>> locationOidsToImageOids = new HashMap<>();

    //relationship between hardware and location
    //Map<Integer, Set<Integer>> hardwareOidsToLocationOids = new HashMap<>();
    Map<Integer, Set<Integer>> locationOidsToHardwareOids = new HashMap<>();

    //relationship between hardware and price
    Map<Integer, Set<Integer>> hardwareOidsToPrice = new HashMap<>();
    //Map<Integer, Set<Integer>> priceToHardwareOids = new HashMap<>();

    //relationship between image and price
    Map<Integer, Set<Integer>> imageOidsToPrice = new HashMap<>();
    //Map<Integer, Set<Integer>> priceToImageOids = new HashMap<>();

    //relationship between location and price
    Map<Integer, Set<Integer>> locationOidsToPrice = new HashMap<>();
    //Map<Integer, Set<Integer>> priceToLocationOids = new HashMap<>();

    for (NodeCandidate nc : nodesToConsider) {
      //hardware stuff
      int cores = nc.getHardware().getCores().intValue();
      int ram = nc.getHardware().getRam().intValue();
      String hardwareOid = nc.getHardware().getId();
      int mappedHardwareOid = hardwareMapper.applyAsInt(hardwareOid);

      //location stuff
      int country = countryMapper.applyAsInt(nc.getLocation().getCountry());
      String locationOid = nc.getLocation().getId();
      int mappedLocationOid = locationMapper.applyAsInt(locationOid);

      //image stuff
      String imageOid = nc.getImage().getId();
      int mappedImageOid = imageMapper.applyAsInt(imageOid);

      //cloud stuff
      //String cloudOid = nc.getCloud().getId();
      //int mappedCloudOid = cloudMapper.applyAsInt(cloudOid);

      //price stuff
      Double price = nc.getPrice();
      int mappedPrice = priceMapper.applyAsInt(price);

      coreDomain.add(cores);
      ramDomain.add(ram);
      countryDomain.add(country);
      priceDomain.add(mappedPrice);
      imageOidDomain.add(mappedImageOid);
      hardwareOidDomain.add(mappedHardwareOid);
      locationOidDomain.add(mappedLocationOid);

      //hardware map creation
      if (!coresToOid.containsKey(cores)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedHardwareOid);
        coresToOid.put(cores, ids);
      } else {
        coresToOid.get(cores)
            .add(mappedHardwareOid);
      }

      if (!ramToOid.containsKey(ram)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedHardwareOid);
        ramToOid.put(ram, ids);
      } else {
        ramToOid.get(ram)
            .add(mappedHardwareOid);
      }

      //hardwareOidsToCores.put(mappedHardwareOid, cores);
      //hardwareOidsToRam.put(mappedHardwareOid, ram);

      //location map creation
      if (!countryToOid.containsKey(country)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedLocationOid);
        countryToOid.put(country, ids);
      } else {
        countryToOid.get(country).add(mappedLocationOid);
      }

      //locationOidsToCountry.put(mappedLocationOid, country);

/*      //location -> cloud map creation
      if (!locationOidsToCloudOids.containsKey(mappedLocationOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedCloudOid);
        locationOidsToCloudOids.put(mappedLocationOid, ids);
      } else {
        locationOidsToCloudOids.get(mappedLocationOid).add(mappedCloudOid);
      }

      if (!cloudOidsToLocationOids.containsKey(mappedCloudOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedLocationOid);
        cloudOidsToLocationOids.put(mappedCloudOid, ids);
      } else {
        cloudOidsToLocationOids.get(mappedCloudOid).add(mappedLocationOid);
      }

      //hardware -> cloud map creation
      if (!hardwareOidsToCloudOids.containsKey(mappedHardwareOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedCloudOid);
        hardwareOidsToCloudOids.put(mappedHardwareOid, ids);
      } else {
        hardwareOidsToCloudOids.get(mappedHardwareOid).add(mappedCloudOid);
      }

      if (!cloudOidsToHardwareOids.containsKey(mappedCloudOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedHardwareOid);
        cloudOidsToHardwareOids.put(mappedCloudOid, ids);
      } else {
        cloudOidsToHardwareOids.get(mappedCloudOid).add(mappedHardwareOid);
      }

      //image -> cloud map creation
      if (!imageOidsToCloudOids.containsKey(mappedImageOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedCloudOid);
        imageOidsToCloudOids.put(mappedImageOid, ids);
      } else {
        imageOidsToCloudOids.get(mappedImageOid).add(mappedCloudOid);
      }

      if (!cloudOidsToImageOids.containsKey(mappedCloudOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedImageOid);
        cloudOidsToImageOids.put(mappedCloudOid, ids);
      } else {
        cloudOidsToImageOids.get(mappedCloudOid).add(mappedImageOid);
      }*/

      //image -> location map creation
      //if (!imageOidsToLocationOids.containsKey(mappedImageOid)) {
      //  HashSet<Integer> ids = new HashSet<>();
      //  ids.add(mappedLocationOid);
      //  imageOidsToLocationOids.put(mappedImageOid, ids);
      //} else {
      //  imageOidsToLocationOids.get(mappedImageOid).add(mappedLocationOid);
      //}

      if (!locationOidsToImageOids.containsKey(mappedLocationOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedImageOid);
        locationOidsToImageOids.put(mappedLocationOid, ids);
      } else {
        locationOidsToImageOids.get(mappedLocationOid).add(mappedImageOid);
      }

      //hardware -> location map creation
      //if (!hardwareOidsToLocationOids.containsKey(mappedHardwareOid)) {
      //  HashSet<Integer> ids = new HashSet<>();
      //  ids.add(mappedLocationOid);
      //  hardwareOidsToLocationOids.put(mappedHardwareOid, ids);
      //} else {
      //  hardwareOidsToLocationOids.get(mappedHardwareOid).add(mappedLocationOid);
      //}

      if (!locationOidsToHardwareOids.containsKey(mappedLocationOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedHardwareOid);
        locationOidsToHardwareOids.put(mappedLocationOid, ids);
      } else {
        locationOidsToHardwareOids.get(mappedLocationOid).add(mappedHardwareOid);
      }

      //price stuff

      //location and price
      if (!locationOidsToPrice.containsKey(mappedLocationOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedPrice);
        locationOidsToPrice.put(mappedLocationOid, ids);
      } else {
        locationOidsToPrice.get(mappedLocationOid).add(mappedPrice);
      }

      //if (!priceToLocationOids.containsKey(mappedPrice)) {
      //  HashSet<Integer> ids = new HashSet<>();
      //  ids.add(mappedLocationOid);
      //  priceToLocationOids.put(mappedPrice, ids);
      //} else {
      //  priceToLocationOids.get(mappedPrice).add(mappedLocationOid);
      //}

      //hardware and price
      if (!hardwareOidsToPrice.containsKey(mappedHardwareOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedPrice);
        hardwareOidsToPrice.put(mappedHardwareOid, ids);
      } else {
        hardwareOidsToPrice.get(mappedHardwareOid).add(mappedPrice);
      }

      //if (!priceToHardwareOids.containsKey(mappedPrice)) {
      //  HashSet<Integer> ids = new HashSet<>();
      //  ids.add(mappedHardwareOid);
      //  priceToHardwareOids.put(mappedPrice, ids);
      //} else {
      //  priceToHardwareOids.get(mappedPrice).add(mappedHardwareOid);
      //}

      //image and price
      if (!imageOidsToPrice.containsKey(mappedImageOid)) {
        HashSet<Integer> ids = new HashSet<>();
        ids.add(mappedPrice);
        imageOidsToPrice.put(mappedImageOid, ids);
      } else {
        imageOidsToPrice.get(mappedImageOid).add(mappedPrice);
      }

      //if (!priceToImageOids.containsKey(mappedPrice)) {
      //  HashSet<Integer> ids = new HashSet<>();
      //  ids.add(mappedImageOid);
      //  priceToImageOids.put(mappedPrice, ids);
      //} else {
      //  priceToImageOids.get(mappedPrice).add(mappedImageOid);
      //}

    }

    //some checking that the maps are correct
    //checkState(hardwareOidsToCores.size() == hardwareOidsToRam.size());
    //checkState(cloudOidsToHardwareOids.size() == cloudOidsToImageOids.size()
    //    && cloudOidsToHardwareOids.size() == cloudOidsToLocationOids.size());
    //checkState(locationOidsToCloudOids.size() == locationOidsToImageOids.size()
    //    && locationOidsToCloudOids.size() == locationOidsToHardwareOids.size());

    int maxPrice = priceDomain.stream().mapToInt(Number::intValue).max().getAsInt();
    int minPrice = priceDomain.stream().mapToInt(Number::intValue).min().getAsInt();

    //generate constants for already existing nodes
    int e = 0;
    for (NodeCandidate nc : existingSolution.getList()) {
      //hardware variables
      IntVar cores = model
          .intVar("Cores Existing Node " + e, nc.getHardware().getCores().intValue());
      coreVariables[e] = cores;
      IntVar ram = model.intVar("RAM Existing Node " + e, nc.getHardware().getRam().intValue());
      ramVariables[e] = ram;
      IntVar hardwareOid = model.intVar("HARDWARE OID Existing Node " + e,
          hardwareMapper.applyAsInt(nc.getHardware().getId()));
      hardwareOidVariables[e] = hardwareOid;

      //location variables
      IntVar country = model.intVar("Country Existing Node " + e,
          countryMapper.applyAsInt(nc.getLocation().getCountry()));
      countryVariables[e] = country;
      IntVar locationOid = model.intVar("Location OID Existing Node " + e,
          locationMapper.applyAsInt(nc.getLocation().getId()));
      locationOidVariables[e] = locationOid;

      //image variables
      IntVar imageOid = model
          .intVar("Image OID Existing Node " + e, imageMapper.applyAsInt(nc.getImage().getId()));
      imageOidVariables[e] = imageOid;

      //price variables
      IntVar price = model
          .intVar("Price Existing Node " + e, priceMapper.applyAsInt(nc.getPrice()));
      priceVariables[e] = price;
      e++;
    }

    //generate variables
    for (int i = existingNodes; i < numberOfNodes; i++) {
      //hardware variables
      IntVar cores = model
          .intVar("Cores Node " + i, coreDomain.stream().mapToInt(Number::intValue).toArray());
      coreVariables[i] = cores;
      IntVar ram = model
          .intVar("RAM Node " + i, ramDomain.stream().mapToInt(Number::intValue).toArray());
      ramVariables[i] = ram;
      IntVar hardwareOid = model.intVar("HARDWARE OID " + i,
          hardwareOidDomain.stream().mapToInt(Number::intValue).toArray());
      hardwareOidVariables[i] = hardwareOid;

      //location variables
      IntVar country = model
          .intVar("COUNTRY Node " + i, countryDomain.stream().mapToInt(Number::intValue).toArray());
      countryVariables[i] = country;
      IntVar locationOid = model.intVar("LOCATION OID" + i,
          locationOidDomain.stream().mapToInt(Number::intValue).toArray());
      locationOidVariables[i] = locationOid;

      //image variables
      IntVar imageOid = model.intVar("IMAGE OID " + i,
          imageOidDomain.stream().mapToInt(Number::intValue).toArray());
      imageOidVariables[i] = imageOid;

      //cloud variables
      //IntVar cloudOid = model.intVar("CLOUD OID " + i,
      //    cloudOidsToHardwareOids.keySet().stream().mapToInt(Number::intValue).toArray());
      //cloudOidVariables[i] = cloudOid;

      //price variables
      IntVar price = model
          .intVar("PRICE " + i, priceDomain.stream().mapToInt(Number::intValue).toArray());
      priceVariables[i] = price;
    }

    //generate constraints for hardware objects
    for (int i = existingNodes; i < numberOfNodes; i++) {
      IntVar coreVariable = coreVariables[i];
      IntVar ramVariable = ramVariables[i];
      IntVar oidVariable = hardwareOidVariables[i];

      //for (Integer oid : hardwareOidsToCores.keySet()) {
      //  model.ifThen(model.arithm(oidVariable, "=", oid),
      //      model.arithm(coreVariable, "=", hardwareOidsToCores.get(oid)));
      //  model.ifThen(model.arithm(oidVariable, "=", oid),
      //      model.arithm(ramVariable, "=", hardwareOidsToRam.get(oid)));
      //}

      for (Map.Entry<Integer, Set<Integer>> entry : coresToOid.entrySet()) {
        model.ifThen(model.arithm(coreVariable, "=", entry.getKey()), model
            .member(oidVariable, entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }

      for (Map.Entry<Integer, Set<Integer>> entry : ramToOid.entrySet()) {
        model.ifThen(model.arithm(ramVariable, "=", entry.getKey()), model
            .member(oidVariable, entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }
    }

    //generate constraints for location objects
    for (int i = existingNodes; i < numberOfNodes; i++) {
      IntVar countryVariable = countryVariables[i];
      IntVar oidVariable = locationOidVariables[i];

      //for (Integer oid : locationOidsToCountry.keySet()) {
      //  model.ifThen(model.arithm(oidVariable, "=", oid),
      //      model.arithm(countryVariable, "=", locationOidsToCountry.get(oid)));
      //}

      for (Map.Entry<Integer, Set<Integer>> entry : countryToOid.entrySet()) {
        model.ifThen(model.arithm(countryVariable, "=", entry.getKey()), model
            .member(oidVariable, entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }
    }

/*    //generate reference between location and cloud
    for (int i = 0; i < numberOfNodes; i++) {
      IntVar locationOidVariable = locationOidVariables[i];
      IntVar cloudOidVariable = cloudOidVariables[i];

      for (Map.Entry<Integer, Set<Integer>> entry : locationOidsToCloudOids.entrySet()) {
        model.ifThen(model.arithm(locationOidVariable, "=", entry.getKey()), model
            .member(cloudOidVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }

      for (Map.Entry<Integer, Set<Integer>> entry : cloudOidsToLocationOids.entrySet()) {
        model.ifThen(model.arithm(cloudOidVariable, "=", entry.getKey()), model
            .member(locationOidVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }
    }

    //generate reference between image and cloud
    for (int i = 0; i < numberOfNodes; i++) {
      IntVar imageOidVariable = imageOidVariables[i];
      IntVar cloudOidVariable = cloudOidVariables[i];

      for (Map.Entry<Integer, Set<Integer>> entry : imageOidsToCloudOids.entrySet()) {
        model.ifThen(model.arithm(imageOidVariable, "=", entry.getKey()), model
            .member(cloudOidVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }

      for (Map.Entry<Integer, Set<Integer>> entry : cloudOidsToImageOids.entrySet()) {
        model.ifThen(model.arithm(cloudOidVariable, "=", entry.getKey()), model
            .member(imageOidVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }
    }

    //generate reference between hardware and cloud
    for (int i = 0; i < numberOfNodes; i++) {
      IntVar hardwareOidVariable = hardwareOidVariables[i];
      IntVar cloudOidVariable = cloudOidVariables[i];

      for (Map.Entry<Integer, Set<Integer>> entry : imageOidsToCloudOids.entrySet()) {
        model.ifThen(model.arithm(hardwareOidVariable, "=", entry.getKey()), model
            .member(cloudOidVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }

      for (Map.Entry<Integer, Set<Integer>> entry : cloudOidsToHardwareOids.entrySet()) {
        model.ifThen(model.arithm(cloudOidVariable, "=", entry.getKey()), model
            .member(hardwareOidVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }
    }*/

    //generate reference between image and location
    for (int i = existingNodes; i < numberOfNodes; i++) {
      IntVar imageOidVariable = imageOidVariables[i];
      IntVar locationOidVariable = locationOidVariables[i];

      //for (Map.Entry<Integer, Set<Integer>> entry : imageOidsToLocationOids.entrySet()) {
      //  model.ifThen(model.arithm(locationOidVariable, "=", entry.getKey()), model
      //      .member(locationOidVariable,
      //          entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      //}

      for (Map.Entry<Integer, Set<Integer>> entry : locationOidsToImageOids.entrySet()) {
        model.ifThen(model.arithm(locationOidVariable, "=", entry.getKey()), model
            .member(imageOidVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }
    }

    //generate reference between hardware and location
    for (int i = existingNodes; i < numberOfNodes; i++) {
      IntVar hardwareOidVariable = hardwareOidVariables[i];
      IntVar locationOidVariable = locationOidVariables[i];

      //for (Map.Entry<Integer, Set<Integer>> entry : imageOidsToLocationOids.entrySet()) {
      //  model.ifThen(model.arithm(hardwareOidVariable, "=", entry.getKey()), model
      //      .member(locationOidVariable,
      //          entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      //}

      for (Map.Entry<Integer, Set<Integer>> entry : locationOidsToHardwareOids.entrySet()) {
        model.ifThen(model.arithm(locationOidVariable, "=", entry.getKey()), model
            .member(hardwareOidVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }
    }

    //generate price
    for (int i = existingNodes; i < numberOfNodes; i++) {
      IntVar hardwareOidVariable = hardwareOidVariables[i];
      IntVar locationOidVariable = locationOidVariables[i];
      IntVar imageOidVariable = imageOidVariables[i];
      IntVar priceVariable = priceVariables[i];

      for (Map.Entry<Integer, Set<Integer>> entry : imageOidsToPrice.entrySet()) {
        model.ifThen(model.arithm(imageOidVariable, "=", entry.getKey()), model
            .member(priceVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }

      //for (Map.Entry<Integer, Set<Integer>> entry : priceToImageOids.entrySet()) {
      //  model.ifThen(model.arithm(priceVariable, "=", entry.getKey()), model
      //      .member(imageOidVariable,
      //          entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      //}

      for (Map.Entry<Integer, Set<Integer>> entry : hardwareOidsToPrice.entrySet()) {
        model.ifThen(model.arithm(hardwareOidVariable, "=", entry.getKey()), model
            .member(priceVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }

      //for (Map.Entry<Integer, Set<Integer>> entry : priceToHardwareOids.entrySet()) {
      //  model.ifThen(model.arithm(priceVariable, "=", entry.getKey()), model
      //      .member(hardwareOidVariable,
      //          entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      //}

      for (Map.Entry<Integer, Set<Integer>> entry : locationOidsToPrice.entrySet()) {
        model.ifThen(model.arithm(locationOidVariable, "=", entry.getKey()), model
            .member(priceVariable,
                entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      }

      //for (Map.Entry<Integer, Set<Integer>> entry : priceToLocationOids.entrySet()) {
      //  model.ifThen(model.arithm(priceVariable, "=", entry.getKey()), model
      //      .member(locationOidVariable,
      //          entry.getValue().stream().mapToInt(Number::intValue).toArray()));
      //}

    }

    //generate the constraints
    //constraints.add("nodes.hardware.cores->sum() >= 15");
    model.sum(coreVariables, ">=", 15).post();

    //constraints.add("nodes->isUnique(n | n.location.country)");
    model.allDifferent(countryVariables).post();

    //constraints.add("nodes->exists(location.country = 'DE')");
    Set<Constraint> countryConstraints = new HashSet<>();
    for (IntVar countryVariable : countryVariables) {
      countryConstraints.add(countryVariable.eq(countryMapper.applyAsInt("DE")).decompose());
    }
    model.or(countryConstraints.toArray(new Constraint[countryConstraints.size()])).post();

    //constraints.add("nodes->select(n | n.hardware.cores > 4)->size() >= 2");

    IntVar limit = model.intVar(0, numberOfNodes);
    model.arithm(limit, ">=", 2).post();
    IntVar value = model.intVar(coreDomain.stream().mapToInt(Number::intValue).toArray());
    model.arithm(value, ">=", 4).post();
    model.count(value, coreVariables, limit).post();

    IntVar objectiveFunction = model
        .intVar("objective", minPrice * numberOfNodes, maxPrice * numberOfNodes);
    model.sum(priceVariables, "<=", objectiveFunction).post();
    model.setObjective(Model.MINIMIZE, objectiveFunction);

    Solver solver = model.getSolver();
    solver.limitTime(timeLimit.toMillis());

    solver.setSearch(Search.intVarSearch(new VariableSelector<IntVar>() {
      @Override
      public IntVar getVariable(IntVar[] variables) {

        List<IntVar> uninstantiatedVariables = Arrays.stream(variables)
            .filter(v -> !v.isInstantiated())
            .collect(Collectors.toList());

        if (uninstantiatedVariables.isEmpty()) {
          return null;
        }

        return uninstantiatedVariables.stream().min(
            Comparator.comparingInt(IntVar::getValue)).get();
      }
    }, (IntVar integers) -> {
      return integers.getLB();
    }, priceVariables), Search.defaultSearch(model));

    //solver.setSearch(Search.defaultSearch(model));

    //solver.setSearch(Search.activityBasedSearch(model.retrieveIntVars(true)));

    System.out.println("Generated CSP.");

    org.chocosolver.solver.Solution solution = new org.chocosolver.solver.Solution(model);
    while (solver.solve()) {
      solution.record();
    }

    if (solver.getSolutionCount() == 0) {
      Solution empty = Solution.of(Collections.emptyList());
      empty.setTime(solver.getTimeCount());
      empty.setIsOptimal(false);
      return empty;
    }

    System.out.println("Finished solving");

    List<NodeCandidate> solutionNodes = new ArrayList<>();
    for (int i = 0; i < numberOfNodes; i++) {
      String hardwareId = hardwareMapper.applyBack(solution.getIntVal(hardwareOidVariables[i]));
      String locationId = locationMapper.applyBack(solution.getIntVal(locationOidVariables[i]));
      String imageId = imageMapper.applyBack(solution.getIntVal(imageOidVariables[i]));

      final List<NodeCandidate> collect = nodesToConsider.stream()
          .filter(nodeCandidate -> nodeCandidate.getHardware().getId().equals(hardwareId))
          .filter(nodeCandidate -> nodeCandidate.getImage().getId().equals(imageId))
          .filter(nodeCandidate -> nodeCandidate.getLocation().getId().equals(locationId))
          .collect(Collectors.toList());
      checkState(collect.size() == 1, "Could not map solution to node candidate");
      NodeCandidate nodeCandidate = collect.get(0);
      solutionNodes.add(nodeCandidate);
    }

    Solution mySolution = Solution.of(solutionNodes);
    mySolution.setTime(solver.getTimeCount());

    if (solver.isStopCriterionMet()) {
      System.out.println("Solver met stop criterion.");
    }

    if (solver.isObjectiveOptimal()) {
      System.out.println("Found optimal solution.");
      mySolution.setIsOptimal(true);
    } else {
      mySolution.setIsOptimal(false);
    }

    return mySolution;
  }
}
