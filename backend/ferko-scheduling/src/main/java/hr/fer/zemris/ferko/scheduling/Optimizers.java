package hr.fer.zemris.ferko.scheduling;

import java.util.List;

/**
 * Factory and registry for the available metaheuristics, so callers (e.g. the scheduling jobs and
 * the UI) can select and compare algorithms by name without depending on concrete classes.
 */
public final class Optimizers {

  private Optimizers() {}

  /** Identifier of the memetic hybrid (parallel islands + local refinement). */
  public static final String HYBRID = "HYBRID";

  /** Stable identifiers of the base metaheuristics (used for head-to-head comparison). */
  public static List<String> names() {
    return List.of(
        "GENETIC",
        "DIFFERENTIAL_EVOLUTION",
        "MAX_MIN_ANT_SYSTEM",
        "PARTICLE_SWARM",
        "IMMUNE_ALGORITHM",
        "CLONALG");
  }

  /** Identifiers selectable for a single generation run: the base algorithms plus the hybrid. */
  public static List<String> selectable() {
    List<String> all = new java.util.ArrayList<>(names());
    all.add(HYBRID);
    return List.copyOf(all);
  }

  public static Optimizer create(String name, int populationSize, int iterations, long seed) {
    return switch (name) {
      case "GENETIC" -> new GeneticAlgorithm(new GaConfig(populationSize, iterations, 0.05, seed));
      case "DIFFERENTIAL_EVOLUTION" -> new DifferentialEvolution(populationSize, iterations, seed);
      case "MAX_MIN_ANT_SYSTEM" -> new MaxMinAntSystem(populationSize, iterations, seed);
      case "PARTICLE_SWARM" -> new ParticleSwarm(populationSize, iterations, seed);
      case "IMMUNE_ALGORITHM" -> new ImmuneAlgorithm(populationSize, iterations, seed);
      case "CLONALG" -> new Clonalg(populationSize, iterations, seed);
      case HYBRID -> hybrid(populationSize, iterations, seed);
      default -> throw new IllegalArgumentException("Unknown optimizer: " + name);
    };
  }

  /** Convenience factory with sensible default population/iteration budget. */
  public static Optimizer createDefault(String name, long seed) {
    return create(name, 60, 5000, seed);
  }

  /**
   * The memetic hybrid: every base optimizer runs as a parallel island, the best elite is migrated
   * out and then intensified with local hill-climbing. See {@link HybridOptimizer}.
   */
  public static Optimizer hybrid(int populationSize, int iterations, long seed) {
    List<Optimizer> islands =
        names().stream().map(name -> create(name, populationSize, iterations, seed)).toList();
    return new HybridOptimizer(islands);
  }
}
