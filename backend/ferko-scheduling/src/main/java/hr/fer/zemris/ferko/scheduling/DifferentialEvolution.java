package hr.fer.zemris.ferko.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Differential Evolution (DE/rand/1/bin) over a continuous encoding. Each individual is a {@code
 * double[]} whose component {@code g} lives in {@code [0, optionCount(g))}; it is decoded to the
 * discrete representation through {@code clamp(floor(value), 0, optionCount(g) - 1)} for penalty
 * evaluation. For every individual a donor is built from three distinct others as {@code pop[r1] +
 * F * (pop[r2] - pop[r3])}, binomial crossover with probability {@code CR} (plus one guaranteed
 * gene) forms the trial, and greedy selection keeps the trial when it is no worse. Deterministic
 * for a fixed seed.
 */
public final class DifferentialEvolution implements Optimizer {

  private static final String NAME = "DIFFERENTIAL_EVOLUTION";
  private static final int MIN_POPULATION = 4;
  private static final double F = 0.7;
  private static final double CR = 0.9;

  private final int populationSize;
  private final int iterations;
  private final long seed;

  public DifferentialEvolution(int populationSize, int iterations, long seed) {
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
    int genes = problem.geneCount();

    double[][] population = new double[populationSize][genes];
    double[] penalties = new double[populationSize];
    for (int i = 0; i < populationSize; i++) {
      for (int g = 0; g < genes; g++) {
        population[i][g] = rng.nextDouble() * upperBound(problem, g);
      }
      penalties[i] = problem.penalty(decode(population[i], problem));
    }

    int bestIndex = argMin(penalties);
    int[] best = decode(population[bestIndex], problem);
    double bestPenalty = penalties[bestIndex];

    List<Double> history = new ArrayList<>();
    history.add(bestPenalty);
    int sampleEvery = Math.max(1, iterations / 200);

    double[] trial = new double[genes];
    int iteration = 0;
    for (; iteration < iterations && bestPenalty > 0.0; iteration++) {
      for (int i = 0; i < populationSize && bestPenalty > 0.0; i++) {
        int r1 = distinct(rng, populationSize, i);
        int r2 = distinct(rng, populationSize, i, r1);
        int r3 = distinct(rng, populationSize, i, r1, r2);

        int jrand = rng.nextInt(genes);
        for (int g = 0; g < genes; g++) {
          if (g == jrand || rng.nextDouble() < CR) {
            double mutated = population[r1][g] + F * (population[r2][g] - population[r3][g]);
            trial[g] = clamp(mutated, 0.0, upperBound(problem, g));
          } else {
            trial[g] = population[i][g];
          }
        }

        int[] decoded = decode(trial, problem);
        double trialPenalty = problem.penalty(decoded);
        if (trialPenalty <= penalties[i]) {
          System.arraycopy(trial, 0, population[i], 0, genes);
          penalties[i] = trialPenalty;
          if (trialPenalty < bestPenalty) {
            bestPenalty = trialPenalty;
            best = decoded;
          }
        }
      }
      if (iteration % sampleEvery == 0) {
        history.add(bestPenalty);
      }
    }
    history.add(bestPenalty);

    return new OptimizationResult(NAME, best, bestPenalty, iteration, List.copyOf(history));
  }

  private static double upperBound(Problem problem, int gene) {
    return problem.optionCount(gene) - 1e-9;
  }

  private static int[] decode(double[] individual, Problem problem) {
    int[] genes = new int[individual.length];
    for (int g = 0; g < genes.length; g++) {
      int idx = (int) Math.floor(individual[g]);
      genes[g] = clampIndex(idx, problem.optionCount(g) - 1);
    }
    return genes;
  }

  private static int clampIndex(int value, int max) {
    if (value < 0) {
      return 0;
    }
    return Math.min(value, max);
  }

  private static double clamp(double value, double min, double max) {
    if (value < min) {
      return min;
    }
    return Math.min(value, max);
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

  private static int distinct(Random rng, int bound, int... taken) {
    int value;
    boolean clash;
    do {
      value = rng.nextInt(bound);
      clash = false;
      for (int t : taken) {
        if (t == value) {
          clash = true;
          break;
        }
      }
    } while (clash);
    return value;
  }
}
