package hr.fer.zemris.ferko.scheduling;

/**
 * A metaheuristic that searches for a minimal-penalty assignment for a {@link Problem}. All
 * algorithm families from Čupić's thesis (genetic, differential evolution, ant colony, particle
 * swarm, immune) implement this single contract so they are interchangeable and comparable.
 */
public interface Optimizer {

  /** Short stable identifier, e.g. {@code "GENETIC"}, {@code "DIFFERENTIAL_EVOLUTION"}. */
  String name();

  OptimizationResult optimize(Problem problem);
}
