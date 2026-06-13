package hr.fer.zemris.ferko.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * CLONALG clonal selection algorithm over the discrete representation. Antibodies are integer
 * solutions; each iteration sorts them by penalty, clones better-ranked antibodies more heavily
 * (clones = floor(beta * popSize / (rank + 1))), hypermutates each clone with a rate inversely
 * proportional to its normalized fitness (better solutions mutate less), keeps the best {@code
 * populationSize} antibodies, and replaces the {@code newCount} worst with fresh random antibodies.
 * Deterministic for a fixed seed.
 */
public final class Clonalg implements Optimizer {

  /** Algorithm identifier returned by {@link #name()}. */
  public static final String NAME = "CLONALG";

  private static final int MIN_POPULATION = 4;
  private static final double BETA = 1.0;
  private static final int NEW_COUNT = 2;

  private final int populationSize;
  private final int iterations;
  private final long seed;

  /**
   * Creates a CLONALG optimizer.
   *
   * @param populationSize number of antibodies, at least {@value #MIN_POPULATION}
   * @param iterations number of clonal-selection iterations, non-negative
   * @param seed seed for the deterministic random source
   * @throws IllegalArgumentException if the configuration is invalid
   */
  public Clonalg(int populationSize, int iterations, long seed) {
    if (populationSize < MIN_POPULATION) {
      throw new IllegalArgumentException("populationSize must be >= " + MIN_POPULATION);
    }
    if (iterations < 0) {
      throw new IllegalArgumentException("iterations must be >= 0");
    }
    this.populationSize = populationSize;
    this.iterations = iterations;
    this.seed = seed;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public OptimizationResult optimize(Problem problem) {
    Random rng = new Random(seed);

    List<int[]> population = new ArrayList<>(populationSize);
    double[] penalties = new double[populationSize];
    for (int i = 0; i < populationSize; i++) {
      int[] antibody = randomSolution(problem, rng);
      population.add(antibody);
      penalties[i] = problem.penalty(antibody);
    }
    sortByPenalty(population, penalties);

    int[] best = population.get(0).clone();
    double bestPenalty = penalties[0];

    List<Double> history = new ArrayList<>();
    history.add(bestPenalty);
    int sampleEvery = Math.max(1, iterations / 200);

    int iteration = 0;
    for (; iteration < iterations && bestPenalty > 0.0; iteration++) {
      List<int[]> pool = new ArrayList<>(population);
      double worstPenalty = penalties[populationSize - 1];
      double bestOfPool = penalties[0];
      double range = worstPenalty - bestOfPool;

      for (int rank = 0; rank < populationSize; rank++) {
        int cloneCount = (int) Math.floor(BETA * populationSize / (rank + 1.0));
        double normalizedFitness = range <= 0.0 ? 1.0 : (worstPenalty - penalties[rank]) / range;
        double mutationRate = 1.0 - 0.9 * normalizedFitness;
        for (int c = 0; c < cloneCount; c++) {
          int[] clone = population.get(rank).clone();
          hypermutate(clone, problem, mutationRate, rng);
          pool.add(clone);
        }
      }

      double[] poolPenalties = new double[pool.size()];
      for (int i = 0; i < pool.size(); i++) {
        poolPenalties[i] = problem.penalty(pool.get(i));
      }
      sortByPenalty(pool, poolPenalties);

      for (int i = 0; i < populationSize; i++) {
        population.set(i, pool.get(i));
        penalties[i] = poolPenalties[i];
      }

      int replaceFrom = Math.max(0, populationSize - NEW_COUNT);
      for (int i = replaceFrom; i < populationSize; i++) {
        int[] fresh = randomSolution(problem, rng);
        population.set(i, fresh);
        penalties[i] = problem.penalty(fresh);
      }
      sortByPenalty(population, penalties);

      if (penalties[0] < bestPenalty) {
        bestPenalty = penalties[0];
        best = population.get(0).clone();
      }
      if (iteration % sampleEvery == 0) {
        history.add(bestPenalty);
      }
    }
    history.add(bestPenalty);

    return new OptimizationResult(NAME, best, bestPenalty, iteration, List.copyOf(history));
  }

  private static int[] randomSolution(Problem problem, Random rng) {
    int[] solution = new int[problem.geneCount()];
    for (int gene = 0; gene < solution.length; gene++) {
      solution[gene] = rng.nextInt(Math.max(1, problem.optionCount(gene)));
    }
    return solution;
  }

  private static void hypermutate(int[] solution, Problem problem, double rate, Random rng) {
    for (int gene = 0; gene < solution.length; gene++) {
      if (rng.nextDouble() < rate) {
        solution[gene] = rng.nextInt(Math.max(1, problem.optionCount(gene)));
      }
    }
  }

  private static void sortByPenalty(List<int[]> antibodies, double[] penalties) {
    Integer[] order = new Integer[antibodies.size()];
    for (int i = 0; i < order.length; i++) {
      order[i] = i;
    }
    Arrays.sort(order, Comparator.comparingDouble(i -> penalties[i]));

    List<int[]> sortedAntibodies = new ArrayList<>(antibodies.size());
    double[] sortedPenalties = new double[penalties.length];
    for (int i = 0; i < order.length; i++) {
      sortedAntibodies.add(antibodies.get(order[i]));
      sortedPenalties[i] = penalties[order[i]];
    }
    for (int i = 0; i < antibodies.size(); i++) {
      antibodies.set(i, sortedAntibodies.get(i));
      penalties[i] = sortedPenalties[i];
    }
  }
}
