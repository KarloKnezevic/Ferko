package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UnscheduledStudentsProblemTest {

  // Three (student, course) genes:
  //   gene 0: student 0, course A -> capacity {1,5}, slot {10,11}
  //   gene 1: student 0, course B -> capacity {5,5}, slot {10,21}
  //   gene 2: student 1, course A -> capacity {1,5}, slot {10,11}
  // Genes 0 and 2 share course A's option signature, so they compete for the same group capacities.
  private static UnscheduledStudentsProblem tinyProblem() {
    int[][] capacity = {
      {1, 5},
      {5, 5},
      {1, 5}
    };
    int[][] slot = {
      {10, 11},
      {10, 21},
      {10, 11}
    };
    int[] studentOfGene = {0, 0, 1};
    return new UnscheduledStudentsProblem(capacity, slot, studentOfGene, 2.0, 2.0);
  }

  @Test
  void feasibleSolutionHasZeroPenalty() {
    UnscheduledStudentsProblem problem = tinyProblem();
    // gene0 -> A opt1 (cap5, slot11), gene1 -> B opt1 (slot21), gene2 -> A opt0 (cap1, slot10).
    // No course option exceeds capacity; student 0 has no slot clash.
    assertEquals(0.0, problem.penalty(new int[] {1, 1, 0}));
    assertEquals(3, problem.geneCount());
    assertEquals(2, problem.studentCount());
    assertEquals(2, problem.optionCount(0));
  }

  @Test
  void overCapacityOnlyMatchesNonLinearTerm() {
    UnscheduledStudentsProblem problem = tinyProblem();
    // gene0 -> A opt0 (slot10), gene1 -> B opt1 (slot21), gene2 -> A opt0 (slot10).
    // Course A opt0 holds 2 genes vs capacity 1 -> overflow 1 -> 1^2 = 1.
    // Student 0: slot10 vs slot21 -> no overlap. Total penalty exactly 1.
    assertEquals(1.0, problem.penalty(new int[] {0, 1, 0}));
  }

  @Test
  void timeOverlapDominatesAndIsPositive() {
    UnscheduledStudentsProblem problem = tinyProblem();
    // gene0 -> A opt0 (slot10), gene1 -> B opt0 (slot10): student 0 clashes -> hard violation.
    // Plus course A opt0 over capacity (gene0 + gene2) -> soft term 1.
    double penalty = problem.penalty(new int[] {0, 0, 0});
    assertTrue(penalty > 0.0);
    // 1 (over-capacity) + 1_000_000 * 1^2 (one overlapping pair for student 0).
    assertEquals(1_000_001.0, penalty);
  }

  @Test
  void rejectsInvalidConstruction() {
    int[][] capacity = {{1, 5}};
    int[][] slot = {{10, 11}};
    assertThrows(
        IllegalArgumentException.class,
        () -> new UnscheduledStudentsProblem(null, slot, new int[] {0}, 2.0, 2.0));
    // length mismatch between genes and studentOfGene
    assertThrows(
        IllegalArgumentException.class,
        () -> new UnscheduledStudentsProblem(capacity, slot, new int[] {0, 1}, 2.0, 2.0));
    // capacity and slot option counts differ for a gene
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new UnscheduledStudentsProblem(capacity, new int[][] {{10}}, new int[] {0}, 2.0, 2.0));
    // alpha below 1
    assertThrows(
        IllegalArgumentException.class,
        () -> new UnscheduledStudentsProblem(capacity, slot, new int[] {0}, 0.5, 2.0));
    // empty option row
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new UnscheduledStudentsProblem(
                new int[][] {{}}, new int[][] {{}}, new int[] {0}, 2.0, 2.0));
  }
}
