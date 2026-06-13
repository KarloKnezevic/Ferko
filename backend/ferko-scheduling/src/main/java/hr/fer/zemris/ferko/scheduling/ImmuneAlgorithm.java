package hr.fer.zemris.ferko.scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Simple Immune Algorithm (SIA) over the discrete representation. Each antibody is an {@code int[]}
 * solution. Every generation each antibody is cloned {@code beta} times, each clone undergoes a
 * per-gene mutation (a gene is reassigned to a random option with {@code mutationProbability}), and
 * the best {@code populationSize - newCount} members of the union of the old antibodies and their
 * mutated clones survive; the remaining {@code newCount} slots are filled with fresh random
 * antibodies (metadynamics). Deterministic for a fixed seed.
 */
public final class ImmuneAlgorithm implements Optimizer {

  /** Algorithm identifier returned by {@link #name()}. */
  public static final String NAME = "IMMUNE_ALGORITHM";

  private static final int MIN_POPULATION = 4;
  private static final int BETA = 2;
  private static final int NEW_COUNT = 2;
  private static final double MUTATION_PROBABILITY = 0.05;

  private final int populationSize;
  private final int iterations;
  private final long seed;

  /**
   * Creates a Simple Immune Algorithm optimizer.
   *
   * @param populationSize number of antibodies, must be {@code >= 4}
   * @param iterations number of generations, must be {@code >= 0}
   * @param seed seed for the deterministic pseudo-random generator
   */
  public ImmuneAlgorithm(int populationSize, int iterations, long seed) {
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

    int[][] population = new int[populationSize][];
    double[] penalties = new double[populationSize];
    for (int i = 0; i < populationSize; i++) {
      population[i] = randomSolution(problem, rng);
      penalties[i] = problem.penalty(population[i]);
    }

    int bestIndex = argMin(penalties);
    int[] best = population[bestIndex].clone();
    double bestPenalty = penalties[bestIndex];

    List<Double> history = new ArrayList<>();
    history.add(bestPenalty);
    int sampleEvery = Math.max(1, iterations / 200);

    int generation = 0;
    for (; generation < iterations && bestPenalty > 0.0; generation++) {
      List<Member> pool = new ArrayList<>(populationSize * (1 + BETA));
      for (int i = 0; i < populationSize; i++) {
        pool.add(new Member(population[i], penalties[i]));
        for (int c = 0; c < BETA; c++) {
          int[] clone = population[i].clone();
          mutate(clone, problem, rng);
          pool.add(new Member(clone, problem.penalty(clone)));
        }
      }
      pool.sort(Comparator.comparingDouble(m -> m.penalty));

      int survivors = populationSize - NEW_COUNT;
      for (int i = 0; i < survivors; i++) {
        Member m = pool.get(i);
        population[i] = m.genes;
        penalties[i] = m.penalty;
      }
      for (int i = survivors; i < populationSize; i++) {
        population[i] = randomSolution(problem, rng);
        penalties[i] = problem.penalty(population[i]);
      }

      int genBest = argMin(penalties);
      if (penalties[genBest] < bestPenalty) {
        bestPenalty = penalties[genBest];
        best = population[genBest].clone();
      }
      if (generation % sampleEvery == 0) {
        history.add(bestPenalty);
      }
    }
    history.add(bestPenalty);

    return new OptimizationResult(NAME, best, bestPenalty, generation, List.copyOf(history));
  }

  private static int[] randomSolution(Problem problem, Random rng) {
    int[] solution = new int[problem.geneCount()];
    for (int gene = 0; gene < solution.length; gene++) {
      solution[gene] = rng.nextInt(Math.max(1, problem.optionCount(gene)));
    }
    return solution;
  }

  private static void mutate(int[] solution, Problem problem, Random rng) {
    for (int gene = 0; gene < solution.length; gene++) {
      if (rng.nextDouble() < MUTATION_PROBABILITY) {
        solution[gene] = rng.nextInt(Math.max(1, problem.optionCount(gene)));
      }
    }
  }

  private static int argMin(double[] values) {
    int index = 0;
    for (int i = 1; i < values.length; i++) {
      if (values[i] < values[index]) {
        index = i;
      }
    }
    return index;
  }

  private static final class Member {
    private final int[] genes;
    private final double penalty;

    Member(int[] genes, double penalty) {
      this.genes = genes;
      this.penalty = penalty;
    }

    @Override
    public String toString() {
      return penalty + " " + Arrays.toString(genes);
    }
  }
}
