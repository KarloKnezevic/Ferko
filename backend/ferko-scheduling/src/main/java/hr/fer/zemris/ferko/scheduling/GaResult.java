package hr.fer.zemris.ferko.scheduling;

import java.util.List;

/**
 * Result of a genetic-algorithm run.
 *
 * @param assignment best solution found (gene -> option index)
 * @param penalty penalty of the best solution (0 = perfect)
 * @param generationsRun number of elimination steps actually performed
 * @param penaltyHistory best-penalty samples over time (for convergence inspection)
 */
public record GaResult(
    int[] assignment, double penalty, int generationsRun, List<Double> penaltyHistory) {

  public boolean isPerfect() {
    return penalty <= 0.0;
  }
}
