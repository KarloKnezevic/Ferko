package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SeminarGroupsProblemTest {

  @Test
  void feasibleZeroCostAssignmentHasZeroPenalty() {
    // 2 groups, each fits 1 student; both students prefer their own group at cost 0.
    int[] capacity = {1, 1};
    int[][] cost = {
      {0, 5},
      {5, 0},
    };
    SeminarGroupsProblem problem = new SeminarGroupsProblem(capacity, cost);

    assertEquals(2, problem.studentCount());
    assertEquals(2, problem.groupCount());
    assertEquals(2, problem.geneCount());
    assertEquals(2, problem.optionCount(0));
    assertEquals(0.0, problem.penalty(new int[] {0, 1}));
  }

  @Test
  void capacityOverflowDominatesAndIsPositive() {
    int[] capacity = {1, 1};
    int[][] cost = {
      {0, 5},
      {0, 5},
    };
    SeminarGroupsProblem problem = new SeminarGroupsProblem(capacity, cost);

    // Both students placed in group 0 (capacity 1) -> overflow 1, squared, dominating.
    double penalty = problem.penalty(new int[] {0, 0});
    assertTrue(penalty > 0.0);
    assertTrue(penalty > 1_000.0, "capacity violation must dominate preference cost");
  }

  @Test
  void exactValueOnTinyExample() {
    int[] capacity = {1, 2};
    int[][] cost = {
      {3, 7},
      {2, 4},
      {9, 1},
    };
    SeminarGroupsProblem problem = new SeminarGroupsProblem(capacity, cost);

    // Assignment: s0->0, s1->1, s2->1. Group sizes {1,2}, both within capacity -> no overflow.
    // Preference total = 3 + 4 + 1 = 8.
    assertEquals(8.0, problem.penalty(new int[] {0, 1, 1}));

    // Assignment: s0->0, s1->0, s2->0. Group 0 size 3, capacity 1 -> overflow 2 -> 4 squared.
    // Preference total = 3 + 2 + 9 = 14.
    double expected = 1_000_000.0 * 4 + 14.0;
    assertEquals(expected, problem.penalty(new int[] {0, 0, 0}));
  }

  @Test
  void constructorRejectsEmptyGroups() {
    assertThrows(
        IllegalArgumentException.class, () -> new SeminarGroupsProblem(new int[0], new int[0][0]));
  }

  @Test
  void constructorRejectsNullCapacity() {
    assertThrows(
        IllegalArgumentException.class, () -> new SeminarGroupsProblem(null, new int[0][0]));
  }

  @Test
  void constructorRejectsNegativeCapacity() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SeminarGroupsProblem(new int[] {-1}, new int[0][1]));
  }

  @Test
  void constructorRejectsNullPreferenceMatrix() {
    assertThrows(
        IllegalArgumentException.class, () -> new SeminarGroupsProblem(new int[] {1}, null));
  }

  @Test
  void constructorRejectsMisshapedPreferenceRow() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SeminarGroupsProblem(new int[] {1, 1}, new int[][] {{0}}));
  }

  @Test
  void constructorRejectsNegativePreferenceCost() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SeminarGroupsProblem(new int[] {1}, new int[][] {{-1}}));
  }

  @Test
  void inputsAreDefensivelyCopied() {
    int[] capacity = {1, 1};
    int[][] cost = {{0, 5}, {5, 0}};
    SeminarGroupsProblem problem = new SeminarGroupsProblem(capacity, cost);

    capacity[0] = 99;
    cost[0][0] = 99;

    assertEquals(0.0, problem.penalty(new int[] {0, 1}));
  }
}
