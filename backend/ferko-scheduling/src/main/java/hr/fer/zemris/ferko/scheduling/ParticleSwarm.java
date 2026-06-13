package hr.fer.zemris.ferko.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Particle Swarm Optimization over a continuous relaxation of the discrete problem. Each particle
 * keeps a real-valued position and velocity in {@code [0, optionCount(g))} per gene; the velocity
 * is updated with inertia and cognitive/social attraction towards the personal and global bests,
 * and positions are decoded to integers via floor-and-clamp for penalty evaluation. Deterministic
 * for a fixed seed.
 */
public final class ParticleSwarm implements Optimizer {

  private static final String NAME = "PARTICLE_SWARM";
  private static final int MIN_POPULATION = 4;
  private static final double INERTIA = 0.7;
  private static final double COGNITIVE = 1.5;
  private static final double SOCIAL = 1.5;

  private final int populationSize;
  private final int iterations;
  private final long seed;

  public ParticleSwarm(int populationSize, int iterations, long seed) {
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

    double[][] positions = new double[populationSize][genes];
    double[][] velocities = new double[populationSize][genes];
    double[][] personalBest = new double[populationSize][genes];
    double[] personalBestPenalty = new double[populationSize];

    double[] globalBest = new double[genes];
    double globalBestPenalty = Double.POSITIVE_INFINITY;
    int[] globalBestAssignment = new int[genes];

    for (int i = 0; i < populationSize; i++) {
      for (int g = 0; g < genes; g++) {
        double upper = upperBound(problem, g);
        positions[i][g] = rng.nextDouble() * upper;
        velocities[i][g] = (rng.nextDouble() * 2.0 - 1.0) * upper;
      }
      int[] decoded = decode(positions[i], problem);
      double penalty = problem.penalty(decoded);
      System.arraycopy(positions[i], 0, personalBest[i], 0, genes);
      personalBestPenalty[i] = penalty;
      if (penalty < globalBestPenalty) {
        globalBestPenalty = penalty;
        System.arraycopy(positions[i], 0, globalBest, 0, genes);
        globalBestAssignment = decoded;
      }
    }

    List<Double> history = new ArrayList<>();
    history.add(globalBestPenalty);
    int sampleEvery = Math.max(1, iterations / 200);

    int iter = 0;
    for (; iter < iterations && globalBestPenalty > 0.0; iter++) {
      for (int i = 0; i < populationSize; i++) {
        for (int g = 0; g < genes; g++) {
          double r1 = rng.nextDouble();
          double r2 = rng.nextDouble();
          velocities[i][g] =
              INERTIA * velocities[i][g]
                  + COGNITIVE * r1 * (personalBest[i][g] - positions[i][g])
                  + SOCIAL * r2 * (globalBest[g] - positions[i][g]);
          positions[i][g] = clamp(positions[i][g] + velocities[i][g], problem, g);
        }
        int[] decoded = decode(positions[i], problem);
        double penalty = problem.penalty(decoded);
        if (penalty < personalBestPenalty[i]) {
          personalBestPenalty[i] = penalty;
          System.arraycopy(positions[i], 0, personalBest[i], 0, genes);
        }
        if (penalty < globalBestPenalty) {
          globalBestPenalty = penalty;
          System.arraycopy(positions[i], 0, globalBest, 0, genes);
          globalBestAssignment = decoded;
        }
      }
      if (iter % sampleEvery == 0) {
        history.add(globalBestPenalty);
      }
    }
    history.add(globalBestPenalty);

    return new OptimizationResult(
        NAME, globalBestAssignment, globalBestPenalty, iter, List.copyOf(history));
  }

  private static double upperBound(Problem problem, int gene) {
    return Math.max(1, problem.optionCount(gene));
  }

  private static double clamp(double value, Problem problem, int gene) {
    double max = upperBound(problem, gene) - 1e-9;
    if (value < 0.0) {
      return 0.0;
    }
    if (value > max) {
      return max;
    }
    return value;
  }

  private static int[] decode(double[] position, Problem problem) {
    int[] genes = new int[position.length];
    for (int g = 0; g < position.length; g++) {
      int idx = (int) Math.floor(position[g]);
      int last = problem.optionCount(g) - 1;
      if (idx < 0) {
        idx = 0;
      } else if (idx > last) {
        idx = last;
      }
      genes[g] = idx;
    }
    return genes;
  }
}
