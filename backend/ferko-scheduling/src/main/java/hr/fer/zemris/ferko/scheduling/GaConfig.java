package hr.fer.zemris.ferko.scheduling;

/**
 * Configuration of the genetic algorithm.
 *
 * @param populationSize number of individuals in the population
 * @param generations number of steady-state elimination steps
 * @param mutationRate per-gene probability of mutation in a freshly produced child
 * @param seed deterministic RNG seed (reproducible runs)
 */
public record GaConfig(int populationSize, int generations, double mutationRate, long seed) {

  public GaConfig {
    if (populationSize < 3) {
      throw new IllegalArgumentException("populationSize must be >= 3");
    }
    if (generations < 0) {
      throw new IllegalArgumentException("generations must be >= 0");
    }
    if (mutationRate < 0.0 || mutationRate > 1.0) {
      throw new IllegalArgumentException("mutationRate must be in [0, 1]");
    }
  }

  public static GaConfig defaults() {
    return new GaConfig(60, 4000, 0.03, 42L);
  }
}
