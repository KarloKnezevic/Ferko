package hr.fer.zemris.ferko.scheduling;

import java.util.List;

/**
 * Result of an optimization run.
 *
 * @param algorithm the optimizer that produced this result
 * @param assignment best solution found (gene -> option index)
 * @param penalty penalty of the best solution (0 = perfect)
 * @param iterations number of iterations actually performed
 * @param penaltyHistory best-penalty samples over time (for convergence inspection)
 */
public record OptimizationResult(
    String algorithm,
    int[] assignment,
    double penalty,
    int iterations,
    List<Double> penaltyHistory) {

  public boolean isPerfect() {
    return penalty <= 0.0;
  }
}
