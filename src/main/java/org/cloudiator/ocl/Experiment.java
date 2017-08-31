package org.cloudiator.ocl;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;

public class Experiment {

  private final int limit;
  private final int nodeSize;
  private final int repetitions;
  private volatile boolean finished = false;

  public int getLimit() {
    return limit;
  }

  public int getNodeSize() {
    return nodeSize;
  }

  public int getRepetitions() {
    return repetitions;
  }

  DoubleSummaryStatistics costStatistics;
  LongSummaryStatistics timeStatistics;

  private List<Solution> solutions = new ArrayList<>();

  public Experiment(int limit, int nodeSize, int repetions) {
    this.limit = limit;
    this.nodeSize = nodeSize;
    this.repetitions = repetions;
  }

  public void addSolution(Solution solution) {
    if (finished) {
      throw new IllegalStateException();
    }
    this.solutions.add(solution);
  }

  private void generateStatistics() {
    if (!finished) {
      this.finished = true;
      costStatistics = solutions.stream().mapToDouble(s -> s.getCosts()).summaryStatistics();
      timeStatistics = solutions.stream().mapToLong(s -> s.getTime()).summaryStatistics();
    }
  }

  public DoubleSummaryStatistics getCostStatistics() {
    generateStatistics();
    return costStatistics;
  }

  public LongSummaryStatistics timeStatistics() {
    generateStatistics();
    return timeStatistics;
  }

  public void print() {

    System.out.println(
        String.format("Experiment: nodeSize %s, limit %s, repetitions %s", nodeSize, limit,
            repetitions));

    System.out.println("Average Costs: " + getCostStatistics().getAverage());
    System.out.println("Min Costs: " + getCostStatistics().getMin());
    System.out.println("Max Costs: " + getCostStatistics().getMax());

    System.out.println("Average Time: " + timeStatistics().getAverage());
    System.out.println("Min Time: " + timeStatistics().getMin());
    System.out.println("Max Time: " + timeStatistics().getMax());
  }

}
