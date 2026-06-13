package hr.fer.zemris.ferko.application.usecase.exam;

import java.util.List;

/**
 * Result of running a single optimizer over an exam's seating problem, for the algorithm-comparison
 * view (the FERKO "izbor algoritma + usporedni prikaz rezultata").
 *
 * @param algorithm stable optimizer identifier (e.g. {@code GENETIC}, {@code PARTICLE_SWARM})
 * @param penalty best penalty found (0 = feasible, all rooms within capacity)
 * @param iterations iterations actually performed
 * @param feasible whether the best solution respects all room capacities
 * @param durationMillis wall-clock time of the run
 * @param penaltyHistory best-penalty samples over time (convergence curve)
 */
public record AlgorithmRunView(
    String algorithm,
    double penalty,
    int iterations,
    boolean feasible,
    long durationMillis,
    List<Double> penaltyHistory) {}
