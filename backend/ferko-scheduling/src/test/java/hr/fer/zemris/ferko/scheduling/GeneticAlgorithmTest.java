package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GeneticAlgorithmTest {

  @Test
  void findsFeasibleSeatingWhenCapacityIsSufficient() {
    SeatingProblem problem = new SeatingProblem(20, new int[] {10, 10, 10}, 2.0);
    GaResult result = new GeneticAlgorithm(new GaConfig(60, 8000, 0.05, 7L)).solve(problem);

    assertTrue(
        result.isPerfect(), "expected an over-capacity-free seating, got " + result.penalty());
    assertEquals(20, result.assignment().length);
    assertEquals(0.0, problem.penalty(result.assignment()));
  }

  @Test
  void resolvesExamConflictsWhenSlotsAllow() {
    // 6 exams arranged as a 2-colourable cycle; 3 slots are more than enough for 0 conflicts.
    int n = 6;
    int[][] shared = new int[n][n];
    for (int i = 0; i < n; i++) {
      int j = (i + 1) % n;
      shared[i][j] = 5;
      shared[j][i] = 5;
    }
    ExamTimetableProblem problem = new ExamTimetableProblem(n, 3, shared);
    GaResult result = new GeneticAlgorithm(new GaConfig(60, 8000, 0.05, 11L)).solve(problem);

    assertTrue(result.isPerfect(), "expected a conflict-free timetable, got " + result.penalty());
  }

  @Test
  void isDeterministicForAFixedSeed() {
    SeatingProblem problem = new SeatingProblem(30, new int[] {12, 12, 12}, 2.0);
    GaConfig config = new GaConfig(40, 3000, 0.04, 123L);

    GaResult first = new GeneticAlgorithm(config).solve(problem);
    GaResult second = new GeneticAlgorithm(config).solve(problem);

    assertArrayEquals(first.assignment(), second.assignment());
    assertEquals(first.penalty(), second.penalty());
  }

  @Test
  void recordsConvergenceHistory() {
    SeatingProblem problem = new SeatingProblem(40, new int[] {8, 8, 8}, 2.0);
    GaResult result = new GeneticAlgorithm(new GaConfig(50, 2000, 0.05, 5L)).solve(problem);
    assertTrue(result.penaltyHistory().size() >= 2);
    // The cohort (40) exceeds capacity (24): a perfect solution is impossible.
    assertTrue(result.penalty() > 0.0);
  }

  @Test
  void rejectsInvalidConfiguration() {
    assertThrows(IllegalArgumentException.class, () -> new GaConfig(2, 100, 0.1, 1L));
    assertThrows(IllegalArgumentException.class, () -> new GaConfig(10, 100, 1.5, 1L));
    assertThrows(IllegalArgumentException.class, () -> new GaConfig(10, -1, 0.1, 1L));
  }
}
