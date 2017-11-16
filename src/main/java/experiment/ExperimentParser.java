package experiment;

import choco.TimeLimit;
import experiment.Experiment.CloudiatorModelType;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ExperimentParser {

  public static void main(String[] args) throws IOException {
    File input = new File("solutionsLarge");
    File output = new File("dataLarge");

    PrintWriter writer = new PrintWriter(output);

    writer.println(
        "# NodeSize Limit1MinTime Limit1MinCosts Limit5MinTime Limit5MinCosts Limit10MinTime Limit10MinCosts IterativeTime IterativeCosts");

    List<ExperimentLine> lines = parse(input);

    for (int i = 2; i <= 15; i++) {
      ExperimentLine iterative = null;
      String line = " " + i + " ";
      for (ExperimentLine experimentLine : parse(input)) {
        if (experimentLine.nodeSize == i) {
          if (experimentLine.iterative) {
            if (experimentLine.timeLimit
                .equals(new TimeLimit(TimeUnit.MINUTES, 10))) {
              iterative = experimentLine;
            }
          } else {
            line += experimentLine.timeInSeconds + " ";
            line += experimentLine.price + " ";
          }
        }
      }
      if (iterative == null) {
        throw new IllegalStateException();
      }
      line += iterative.timeInSeconds + " ";
      line += iterative.price;
      writer.println(line);
    }

    writer.flush();
    writer.close();

    return;

  }


  public static List<ExperimentLine> parse(File file) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(file));
    ArrayList<ExperimentLine> experimentLines = new ArrayList<>();
    String line;
    while ((line = br.readLine()) != null) {
      experimentLines.add(ExperimentLine.of(line));
    }
    return experimentLines;
  }


  private static class ExperimentLine {

    private final boolean iterative;
    private final TimeLimit timeLimit;
    private CloudiatorModelType cloudiatorModelType;
    private final int nodeSize;
    private final double price;
    private final double timeInSeconds;
    private final boolean optimal;


    private ExperimentLine(boolean iterative, TimeLimit timeLimit,
        CloudiatorModelType cloudiatorModelType, int nodeSize, double price, double timeInSeconds,
        boolean optimal) {
      this.iterative = iterative;
      this.timeLimit = timeLimit;
      this.cloudiatorModelType = cloudiatorModelType;
      this.nodeSize = nodeSize;
      this.price = price;
      this.timeInSeconds = timeInSeconds;
      this.optimal = optimal;
    }

    private static ExperimentLine of(String string) {
      String[] strings = string.split(" ");
      return new ExperimentLine(Boolean.valueOf(strings[0]),
          TimeLimit.from(strings[1] + strings[2]),
          CloudiatorModelType.valueOf(strings[3]),
          Integer.valueOf(strings[4]), Double.valueOf(strings[5]), Double.valueOf(strings[6]),
          Boolean.valueOf(strings[7]));
    }

    @Override
    public String toString() {
      return "ExperimentLine{" +
          "iterative=" + iterative +
          ", timeLimit=" + timeLimit +
          ", cloudiatorModelType=" + cloudiatorModelType +
          ", nodeSize=" + nodeSize +
          ", price=" + price +
          ", timeInSeconds=" + timeInSeconds +
          ", optimal=" + optimal +
          '}';
    }
  }
}
