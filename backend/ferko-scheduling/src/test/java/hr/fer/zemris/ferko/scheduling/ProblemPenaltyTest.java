package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProblemPenaltyTest {

  @Test
  void seatingPenaltyIsNonLinearOverCapacity() {
    SeatingProblem problem = new SeatingProblem(5, new int[] {2, 10}, 2.0);
    // Put all 5 students into room 0 (capacity 2): overflow 3 -> 3^2 = 9.
    assertEquals(9.0, problem.penalty(new int[] {0, 0, 0, 0, 0}));
    // A feasible split has no overflow.
    assertEquals(0.0, problem.penalty(new int[] {0, 0, 1, 1, 1}));
    assertEquals(2, problem.roomCount());
    assertEquals(12, problem.totalCapacity());
  }

  @Test
  void timetablePenaltyCountsSharedStudentsInSameSlot() {
    int[][] shared = {
      {0, 4, 1},
      {4, 0, 0},
      {1, 0, 0}
    };
    ExamTimetableProblem problem = new ExamTimetableProblem(3, 2, shared);
    // Exams 0 and 1 in slot 0 -> 4 conflicts; exam 2 in slot 1 -> none with 0.
    assertEquals(4.0, problem.penalty(new int[] {0, 0, 1}));
    // All in distinct-enough slots: 0 & 2 differ, 0 & 1 differ -> 0 conflicts.
    assertEquals(0.0, problem.penalty(new int[] {0, 1, 1}));
  }

  @Test
  void rejectsInvalidConstruction() {
    assertThrows(IllegalArgumentException.class, () -> new SeatingProblem(5, new int[] {}, 2.0));
    assertThrows(IllegalArgumentException.class, () -> new SeatingProblem(5, new int[] {3}, 0.5));
    assertThrows(
        IllegalArgumentException.class, () -> new ExamTimetableProblem(2, 1, new int[1][1]));
  }
}
