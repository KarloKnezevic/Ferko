package hr.fer.zemris.ferko.scheduling;

import java.util.ArrayList;
import java.util.List;

/**
 * Memetic hybrid that combines the coarse-grained parallel ("island") metaheuristics with a local
 * search refinement. It first explores in parallel via {@link IslandOptimizer} — every supported
 * algorithm family runs as an independent island and the best (migrated) elite is kept — and then
 * intensifies that elite with greedy single-gene hill-climbing until no single move improves the
 * penalty. This pairs the global exploration of population methods with local exploitation, in the
 * spirit of the hybrid/parallel schemes in chapter 6 of Čupić's thesis.
 */
public final class HybridOptimizer implements Optimizer {

  /** Safety cap on the number of full local-search sweeps. */
  private static final int MAX_SWEEPS = 50;

  private final IslandOptimizer islands;

  public HybridOptimizer(List<Optimizer> islands) {
    this.islands = new IslandOptimizer(islands);
  }

  @Override
  public String name() {
    return "HYBRID";
  }

  @Override
  public OptimizationResult optimize(Problem problem) {
    OptimizationResult base = islands.optimize(problem);
    int[] genes = base.assignment().clone();
    double penalty = problem.penalty(genes);
    List<Double> history = new ArrayList<>(base.penaltyHistory());

    boolean improved = true;
    int sweeps = 0;
    while (improved && sweeps < MAX_SWEEPS && penalty > 0.0) {
      improved = false;
      sweeps++;
      for (int gene = 0; gene < problem.geneCount(); gene++) {
        int current = genes[gene];
        int bestOption = current;
        double bestPenalty = penalty;
        int options = problem.optionCount(gene);
        for (int option = 0; option < options; option++) {
          if (option == current) {
            continue;
          }
          genes[gene] = option;
          double candidate = problem.penalty(genes);
          if (candidate < bestPenalty) {
            bestPenalty = candidate;
            bestOption = option;
          }
        }
        genes[gene] = bestOption;
        if (bestOption != current) {
          penalty = bestPenalty;
          improved = true;
        }
      }
      history.add(penalty);
    }
    return new OptimizationResult(name(), genes, penalty, base.iterations() + sweeps, history);
  }
}
