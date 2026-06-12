package hr.fer.zemris.ferko.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Steady-state (elimination) genetic algorithm, after Čupić's thesis: in each step three random
 * individuals are drawn, the worst is eliminated and replaced by the mutated uniform crossover of
 * the other two. Deterministic for a fixed {@link GaConfig#seed()}.
 */
public final class GeneticAlgorithm {

  private final GaConfig config;

  public GeneticAlgorithm(GaConfig config) {
    this.config = config;
  }

  public GaResult solve(Problem problem) {
    Random rng = new Random(config.seed());
    int genes = problem.geneCount();
    int popSize = config.populationSize();

    int[][] population = new int[popSize][];
    double[] penalties = new double[popSize];
    for (int i = 0; i < popSize; i++) {
      population[i] = randomSolution(problem, rng);
      penalties[i] = problem.penalty(population[i]);
    }

    int bestIndex = argMin(penalties);
    int[] best = population[bestIndex].clone();
    double bestPenalty = penalties[bestIndex];

    List<Double> history = new ArrayList<>();
    history.add(bestPenalty);
    int sampleEvery = Math.max(1, config.generations() / 200);

    int generation = 0;
    for (; generation < config.generations() && bestPenalty > 0.0; generation++) {
      int a = rng.nextInt(popSize);
      int b = distinct(rng, popSize, a);
      int c = distinct(rng, popSize, a, b);

      int worst = worstOf(penalties, a, b, c);
      int parent1 = (worst == a) ? b : a;
      int parent2 = (worst == c) ? b : c;
      if (parent2 == parent1) {
        parent2 = (worst == a) ? c : a;
      }

      int[] child = crossover(population[parent1], population[parent2], rng);
      mutate(child, problem, rng);
      double childPenalty = problem.penalty(child);

      population[worst] = child;
      penalties[worst] = childPenalty;

      if (childPenalty < bestPenalty) {
        bestPenalty = childPenalty;
        best = child.clone();
      }
      if (generation % sampleEvery == 0) {
        history.add(bestPenalty);
      }
    }
    history.add(bestPenalty);

    return new GaResult(best, bestPenalty, generation, List.copyOf(history));
  }

  private static int[] randomSolution(Problem problem, Random rng) {
    int[] solution = new int[problem.geneCount()];
    for (int gene = 0; gene < solution.length; gene++) {
      solution[gene] = rng.nextInt(Math.max(1, problem.optionCount(gene)));
    }
    return solution;
  }

  private int[] crossover(int[] parent1, int[] parent2, Random rng) {
    int[] child = new int[parent1.length];
    for (int gene = 0; gene < child.length; gene++) {
      child[gene] = rng.nextBoolean() ? parent1[gene] : parent2[gene];
    }
    return child;
  }

  private void mutate(int[] solution, Problem problem, Random rng) {
    for (int gene = 0; gene < solution.length; gene++) {
      if (rng.nextDouble() < config.mutationRate()) {
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

  private static int worstOf(double[] penalties, int a, int b, int c) {
    int worst = a;
    if (penalties[b] > penalties[worst]) {
      worst = b;
    }
    if (penalties[c] > penalties[worst]) {
      worst = c;
    }
    return worst;
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
