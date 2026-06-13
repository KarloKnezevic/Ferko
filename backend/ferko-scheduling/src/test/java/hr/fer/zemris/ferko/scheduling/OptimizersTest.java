package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OptimizersTest {

  @Test
  void registryCreatesEveryNamedOptimizer() {
    assertEquals(6, Optimizers.names().size());
    for (String name : Optimizers.names()) {
      Optimizer optimizer = Optimizers.create(name, 20, 200, 1L);
      assertEquals(name, optimizer.name());
    }
  }

  @Test
  void hybridCombinesAllFamiliesAndSolvesFeasibleProblem() {
    SeatingProblem problem = new SeatingProblem(15, new int[] {8, 8, 8}, 2.0);
    OptimizationResult result = Optimizers.hybrid(40, 5000, 3L).optimize(problem);
    assertTrue(
        result.isPerfect(), "hybrid should find a feasible seating, got " + result.penalty());
  }

  @Test
  void rejectsUnknownOptimizer() {
    assertThrows(IllegalArgumentException.class, () -> Optimizers.createDefault("NOPE", 1L));
  }
}
