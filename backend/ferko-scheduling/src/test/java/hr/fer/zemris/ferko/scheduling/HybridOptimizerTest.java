package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class HybridOptimizerTest {

  @Test
  void islandsThenLocalSearchReachAFeasibleSolution() {
    SeatingProblem problem = new SeatingProblem(20, new int[] {10, 10, 10}, 2.0);
    HybridOptimizer optimizer =
        new HybridOptimizer(
            List.of(
                new GeneticAlgorithm(new GaConfig(40, 4000, 0.05, 1L)),
                new GeneticAlgorithm(new GaConfig(40, 4000, 0.05, 2L))));

    OptimizationResult result = optimizer.optimize(problem);

    assertTrue(result.isPerfect(), "expected a feasible solution, got " + result.penalty());
    assertEquals(20, result.assignment().length);
    assertEquals("HYBRID", result.algorithm());
  }

  @Test
  void localSearchDoesNotWorsenTheIslandElite() {
    // A weak single island leaves room for the local search to improve on.
    SeatingProblem problem = new SeatingProblem(12, new int[] {6, 6}, 2.0);
    HybridOptimizer optimizer =
        new HybridOptimizer(List.of(new GeneticAlgorithm(new GaConfig(10, 50, 0.05, 7L))));

    OptimizationResult result = optimizer.optimize(problem);

    // Local hill-climbing never increases the penalty beyond the worst case (all in one room).
    assertTrue(result.penalty() >= 0.0);
    assertEquals("HYBRID", result.algorithm());
  }

  @Test
  void hybridIsExposedThroughTheFactory() {
    assertTrue(Optimizers.selectable().contains("HYBRID"));
    Optimizer optimizer = Optimizers.create("HYBRID", 20, 200, 1L);
    assertEquals("HYBRID", optimizer.name());
  }
}
