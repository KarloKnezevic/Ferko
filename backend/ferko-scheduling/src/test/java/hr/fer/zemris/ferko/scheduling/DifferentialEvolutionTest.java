package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DifferentialEvolutionTest {

  @Test
  void findsFeasibleSeatingWhenCapacityIsSufficient() {
    SeatingProblem problem = new SeatingProblem(20, new int[] {10, 10, 10}, 2.0);
    OptimizationResult result = new DifferentialEvolution(40, 8000, 7L).optimize(problem);

    assertTrue(
        result.isPerfect(), "expected an over-capacity-free seating, got " + result.penalty());
    assertEquals(20, result.assignment().length);
    assertEquals(0.0, problem.penalty(result.assignment()));
    assertTrue(result.penaltyHistory().size() >= 2);
    assertEquals(result.penalty(), result.penaltyHistory().get(result.penaltyHistory().size() - 1));
  }

  @Test
  void isDeterministicForAFixedSeed() {
    SeatingProblem problem = new SeatingProblem(30, new int[] {12, 12, 12}, 2.0);

    OptimizationResult first = new DifferentialEvolution(40, 3000, 123L).optimize(problem);
    OptimizationResult second = new DifferentialEvolution(40, 3000, 123L).optimize(problem);

    assertArrayEquals(first.assignment(), second.assignment());
    assertEquals(first.penalty(), second.penalty());
  }

  @Test
  void rejectsInvalidConfiguration() {
    assertThrows(IllegalArgumentException.class, () -> new DifferentialEvolution(3, 100, 1L));
    assertThrows(IllegalArgumentException.class, () -> new DifferentialEvolution(10, -1, 1L));
  }
}
