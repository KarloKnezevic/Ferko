package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class IslandOptimizerTest {

  @Test
  void runsIslandsConcurrentlyAndReturnsBest() {
    SeatingProblem problem = new SeatingProblem(20, new int[] {10, 10, 10}, 2.0);
    IslandOptimizer optimizer =
        new IslandOptimizer(
            List.of(
                new GeneticAlgorithm(new GaConfig(40, 6000, 0.05, 1L)),
                new GeneticAlgorithm(new GaConfig(40, 6000, 0.05, 2L)),
                new GeneticAlgorithm(new GaConfig(40, 6000, 0.05, 3L))));

    OptimizationResult result = optimizer.optimize(problem);

    assertTrue(result.isPerfect(), "expected a feasible seating, got " + result.penalty());
    assertEquals(20, result.assignment().length);
    assertEquals("PARALLEL_ISLAND", result.algorithm());
  }

  @Test
  void rejectsEmptyIslandList() {
    assertThrows(IllegalArgumentException.class, () -> new IslandOptimizer(List.of()));
  }
}
