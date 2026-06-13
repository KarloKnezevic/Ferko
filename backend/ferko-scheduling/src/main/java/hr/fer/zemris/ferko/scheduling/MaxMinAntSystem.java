package hr.fer.zemris.ferko.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * MAX-MIN Ant System optimizer over the discrete gene representation. A pheromone matrix {@code
 * tau[gene][option]} biases each ant's per-gene choice with probability proportional to {@code
 * tau^alpha} (uniform heuristic). After every colony pass the pheromone evaporates ({@code tau *=
 * (1 - rho)}) and is reinforced only along the best-so-far solution; trails are then clamped to
 * {@code [tauMin, tauMax]} as prescribed by Stützle and Hoos. Deterministic for a fixed seed.
 */
public final class MaxMinAntSystem implements Optimizer {

  private static final String NAME = "MAX_MIN_ANT_SYSTEM";
  private static final int MIN_ANTS = 4;
  private static final double ALPHA = 1.0;
  private static final double RHO = 0.1;
  private static final int HISTORY_SAMPLES = 200;

  private final int populationSize;
  private final int iterations;
  private final long seed;

  public MaxMinAntSystem(int populationSize, int iterations, long seed) {
    if (populationSize < MIN_ANTS) {
      throw new IllegalArgumentException("populationSize must be >= " + MIN_ANTS);
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

    double avgOptions = averageOptions(problem, genes);
    double[][] tau = new double[genes][];
    for (int gene = 0; gene < genes; gene++) {
      tau[gene] = new double[Math.max(1, problem.optionCount(gene))];
    }

    int[] best = randomSolution(problem, genes, rng);
    double bestPenalty = problem.penalty(best);
    initPheromone(tau, bestPenalty);

    List<Double> history = new ArrayList<>();
    history.add(bestPenalty);
    int sampleEvery = Math.max(1, iterations / HISTORY_SAMPLES);

    int iterationsRun = 0;
    for (; iterationsRun < iterations && bestPenalty > 0.0; iterationsRun++) {
      for (int ant = 0; ant < populationSize; ant++) {
        int[] solution = buildSolution(problem, tau, genes, rng);
        double penalty = problem.penalty(solution);
        if (penalty < bestPenalty) {
          bestPenalty = penalty;
          best = solution;
        }
      }
      updatePheromone(tau, best, bestPenalty, avgOptions);
      if (iterationsRun % sampleEvery == 0) {
        history.add(bestPenalty);
      }
    }
    history.add(bestPenalty);

    return new OptimizationResult(
        NAME, best.clone(), bestPenalty, iterationsRun, List.copyOf(history));
  }

  private static double averageOptions(Problem problem, int genes) {
    if (genes == 0) {
      return 1.0;
    }
    long total = 0;
    for (int gene = 0; gene < genes; gene++) {
      total += Math.max(1, problem.optionCount(gene));
    }
    return Math.max(1.0, (double) total / genes);
  }

  private static void initPheromone(double[][] tau, double bestPenalty) {
    double tauMax = tauMax(bestPenalty);
    for (double[] row : tau) {
      java.util.Arrays.fill(row, tauMax);
    }
  }

  private int[] buildSolution(Problem problem, double[][] tau, int genes, Random rng) {
    int[] solution = new int[genes];
    for (int gene = 0; gene < genes; gene++) {
      double[] row = tau[gene];
      int options = row.length;
      double sum = 0.0;
      for (int option = 0; option < options; option++) {
        sum += weight(row[option]);
      }
      double threshold = rng.nextDouble() * sum;
      int chosen = options - 1;
      double cumulative = 0.0;
      for (int option = 0; option < options; option++) {
        cumulative += weight(row[option]);
        if (cumulative >= threshold) {
          chosen = option;
          break;
        }
      }
      solution[gene] = chosen;
    }
    return solution;
  }

  private static double weight(double tauValue) {
    return ALPHA == 1.0 ? tauValue : Math.pow(tauValue, ALPHA);
  }

  private static void updatePheromone(
      double[][] tau, int[] best, double bestPenalty, double avgOptions) {
    double tauMax = tauMax(bestPenalty);
    double tauMin = tauMax / (2.0 * avgOptions);
    double deltaTau = 1.0 / (1.0 + bestPenalty);
    for (int gene = 0; gene < tau.length; gene++) {
      double[] row = tau[gene];
      for (int option = 0; option < row.length; option++) {
        double value = row[option] * (1.0 - RHO);
        if (option == best[gene]) {
          value += deltaTau;
        }
        if (value > tauMax) {
          value = tauMax;
        } else if (value < tauMin) {
          value = tauMin;
        }
        row[option] = value;
      }
    }
  }

  private static double tauMax(double bestPenalty) {
    return 1.0 / ((1.0 - RHO) * (1.0 + bestPenalty));
  }

  private static int[] randomSolution(Problem problem, int genes, Random rng) {
    int[] solution = new int[genes];
    for (int gene = 0; gene < genes; gene++) {
      solution[gene] = rng.nextInt(Math.max(1, problem.optionCount(gene)));
    }
    return solution;
  }
}
