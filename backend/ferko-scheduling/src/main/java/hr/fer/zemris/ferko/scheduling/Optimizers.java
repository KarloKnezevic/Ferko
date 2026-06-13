package hr.fer.zemris.ferko.scheduling;

import java.util.List;

/**
 * Factory and registry for the available metaheuristics, so callers (e.g. the scheduling jobs and
 * the UI) can select and compare algorithms by name without depending on concrete classes.
 */
public final class Optimizers {

  private Optimizers() {}

  /** Stable identifiers of all supported optimizers. */
  public static List<String> names() {
    return List.of(
        "GENETIC",
        "DIFFERENTIAL_EVOLUTION",
        "MAX_MIN_ANT_SYSTEM",
        "PARTICLE_SWARM",
        "IMMUNE_ALGORITHM",
        "CLONALG");
  }

  public static Optimizer create(String name, int populationSize, int iterations, long seed) {
    return switch (name) {
      case "GENETIC" -> new GeneticAlgorithm(new GaConfig(populationSize, iterations, 0.05, seed));
      case "DIFFERENTIAL_EVOLUTION" -> new DifferentialEvolution(populationSize, iterations, seed);
      case "MAX_MIN_ANT_SYSTEM" -> new MaxMinAntSystem(populationSize, iterations, seed);
      case "PARTICLE_SWARM" -> new ParticleSwarm(populationSize, iterations, seed);
      case "IMMUNE_ALGORITHM" -> new ImmuneAlgorithm(populationSize, iterations, seed);
      case "CLONALG" -> new Clonalg(populationSize, iterations, seed);
      default -> throw new IllegalArgumentException("Unknown optimizer: " + name);
    };
  }

  /** Convenience factory with sensible default population/iteration budget. */
  public static Optimizer createDefault(String name, long seed) {
    return create(name, 60, 5000, seed);
  }

  /** A parallel hybrid running every supported optimizer as an island and keeping the best. */
  public static Optimizer hybrid(int populationSize, int iterations, long seed) {
    List<Optimizer> islands =
        names().stream().map(name -> create(name, populationSize, iterations, seed)).toList();
    return new IslandOptimizer(islands);
  }
}
