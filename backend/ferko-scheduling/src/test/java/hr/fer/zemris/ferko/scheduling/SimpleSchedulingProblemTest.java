package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimpleSchedulingProblemTest {

  /** Builds the conflict matrix for a triangle: 0-1, 1-2 and 0-2 all conflict. */
  private static boolean[][] triangle() {
    return new boolean[][] {
      {false, true, true},
      {true, false, true},
      {true, true, false}
    };
  }

  @Test
  void feasibleScheduleHasZeroPenalty() {
    SimpleSchedulingProblem problem = new SimpleSchedulingProblem(3, triangle());
    // Each of the three mutually conflicting activities in its own slot.
    assertEquals(0.0, problem.penalty(new int[] {0, 1, 2}));
    assertEquals(3, problem.geneCount());
    assertEquals(3, problem.activityCount());
    assertEquals(3, problem.timeSlotCount());
    assertEquals(3, problem.optionCount(0));
  }

  @Test
  void conflictingScheduleHasPositivePenalty() {
    SimpleSchedulingProblem problem = new SimpleSchedulingProblem(3, triangle());
    // Activities 0 and 1 share slot 0 -> exactly one violated pair.
    assertEquals(1.0, problem.penalty(new int[] {0, 0, 2}));
    assertTrue(problem.penalty(new int[] {0, 0, 2}) > 0.0);
  }

  @Test
  void allInSameSlotCountsEveryConflictingPair() {
    SimpleSchedulingProblem problem = new SimpleSchedulingProblem(3, triangle());
    // All three in slot 0: pairs (0,1), (0,2), (1,2) -> 3 violations.
    assertEquals(3.0, problem.penalty(new int[] {0, 0, 0}));
  }

  @Test
  void nonConflictingActivitiesMayShareSlotWithoutPenalty() {
    boolean[][] conflict = {
      {false, true, false},
      {true, false, false},
      {false, false, false}
    };
    SimpleSchedulingProblem problem = new SimpleSchedulingProblem(2, conflict);
    // Activities 0 and 2 do not conflict, so sharing slot 0 is fine; 1 sits in slot 1.
    assertEquals(0.0, problem.penalty(new int[] {0, 1, 0}));
    // Activities 0 and 1 conflict and share slot 0 -> one violation.
    assertEquals(1.0, problem.penalty(new int[] {0, 0, 0}));
  }

  @Test
  void constructorIsDefensiveAgainstCallerMutation() {
    boolean[][] conflict = triangle();
    SimpleSchedulingProblem problem = new SimpleSchedulingProblem(3, conflict);
    conflict[0][1] = false;
    conflict[1][0] = false;
    // The internal copy still records the 0-1 conflict.
    assertEquals(1.0, problem.penalty(new int[] {0, 0, 2}));
  }

  @Test
  void rejectsInvalidConstruction() {
    assertThrows(
        IllegalArgumentException.class, () -> new SimpleSchedulingProblem(0, new boolean[0][0]));
    assertThrows(IllegalArgumentException.class, () -> new SimpleSchedulingProblem(2, null));
    // Non-square matrix.
    assertThrows(
        IllegalArgumentException.class,
        () -> new SimpleSchedulingProblem(2, new boolean[][] {{false, true}}));
    // Self-conflict on the diagonal.
    assertThrows(
        IllegalArgumentException.class,
        () -> new SimpleSchedulingProblem(2, new boolean[][] {{true, false}, {false, false}}));
    // Asymmetric matrix.
    assertThrows(
        IllegalArgumentException.class,
        () -> new SimpleSchedulingProblem(2, new boolean[][] {{false, true}, {false, false}}));
  }

  @Test
  void rejectsGeneVectorOfWrongLength() {
    SimpleSchedulingProblem problem = new SimpleSchedulingProblem(3, triangle());
    assertThrows(IllegalArgumentException.class, () -> problem.penalty(new int[] {0, 1}));
    assertThrows(IllegalArgumentException.class, () -> problem.penalty(null));
  }
}
