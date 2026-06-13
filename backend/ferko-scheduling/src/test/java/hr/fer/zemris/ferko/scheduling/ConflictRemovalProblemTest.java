package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConflictRemovalProblemTest {

  /** Student 0 owns genes 0 and 1, each choosing between slot 0 and slot 1. Gene 2 is student 1. */
  private static int[][] groupSlot() {
    return new int[][] {{0, 1}, {0, 1}, {0}};
  }

  @Test
  void perfectSolutionHasZeroPenalty() {
    // Initial assignment is already conflict-free (gene0 slot0, gene1 slot1) and unchanged.
    ConflictRemovalProblem problem =
        new ConflictRemovalProblem(
            groupSlot(),
            new int[] {0, 0, 1},
            new int[] {0, 1, 0},
            new boolean[] {false, false, false});

    assertEquals(0.0, problem.penalty(new int[] {0, 1, 0}));
  }

  @Test
  void conflictingSolutionHasPositivePenalty() {
    // Both genes of student 0 resolve to slot 0 -> one remaining conflict, no changes.
    ConflictRemovalProblem problem =
        new ConflictRemovalProblem(
            groupSlot(),
            new int[] {0, 0, 1},
            new int[] {0, 0, 0},
            new boolean[] {true, true, false});

    double value = problem.penalty(new int[] {0, 0, 0});
    assertTrue(value > 0.0);
    assertEquals(1000.0, value);
  }

  @Test
  void exactValueCombinesConflictsAndWeightedChanges() {
    // Initial conflicting assignment {0,0,0}; both genes of student 0 marked as conflict genes.
    ConflictRemovalProblem problem =
        new ConflictRemovalProblem(
            groupSlot(),
            new int[] {0, 0, 1},
            new int[] {0, 0, 0},
            new boolean[] {true, true, false});

    // Solution {1, 0, 0}: gene0 -> slot1, gene1 -> slot0 (no conflict).
    // Changes: gene0 changed (a conflict gene) -> changesOnConflictGenes = 1.
    // Penalty = 1000*0 + 1*0^2 + 1*1^2 = 1.
    assertEquals(1.0, problem.penalty(new int[] {1, 0, 0}));
  }

  @Test
  void nonConflictChangesAreWeightedSeparatelyAndNonLinearly() {
    // Two students, four single-slot genes, all distinct slots so never any conflict.
    ConflictRemovalProblem problem =
        new ConflictRemovalProblem(
            new int[][] {{0, 5}, {1, 6}, {2, 7}, {3, 8}},
            new int[] {0, 0, 1, 1},
            new int[] {0, 0, 0, 0},
            new boolean[] {false, false, true, true},
            1000.0,
            1.0,
            1.0);

    // Change all four genes: 2 non-conflict genes, 2 conflict genes.
    // Penalty = 1000*0 + 1*2^2 + 1*2^2 = 8.
    assertEquals(8.0, problem.penalty(new int[] {1, 1, 1, 1}));
  }

  @Test
  void encodingMetadataMatchesInputs() {
    ConflictRemovalProblem problem =
        new ConflictRemovalProblem(
            groupSlot(),
            new int[] {0, 0, 1},
            new int[] {0, 1, 0},
            new boolean[] {false, false, false});

    assertEquals(3, problem.geneCount());
    assertEquals(2, problem.optionCount(0));
    assertEquals(1, problem.optionCount(2));
    assertEquals(2, problem.studentCount());
  }

  @Test
  void constructorRejectsNullGroupSlot() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ConflictRemovalProblem(null, new int[] {0}, new int[] {0}, new boolean[] {false}));
  }

  @Test
  void constructorRejectsMismatchedLengths() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ConflictRemovalProblem(
                new int[][] {{0}}, new int[] {0, 0}, new int[] {0}, new boolean[] {false}));
  }

  @Test
  void constructorRejectsInitialAssignmentOutOfRange() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ConflictRemovalProblem(
                new int[][] {{0, 1}}, new int[] {0}, new int[] {2}, new boolean[] {false}));
  }

  @Test
  void constructorRejectsNegativeWeights() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ConflictRemovalProblem(
                new int[][] {{0}},
                new int[] {0},
                new int[] {0},
                new boolean[] {false},
                -1.0,
                1.0,
                1.0));
  }

  @Test
  void penaltyRejectsWrongGeneLength() {
    ConflictRemovalProblem problem =
        new ConflictRemovalProblem(
            groupSlot(),
            new int[] {0, 0, 1},
            new int[] {0, 1, 0},
            new boolean[] {false, false, false});

    assertThrows(IllegalArgumentException.class, () -> problem.penalty(new int[] {0, 0}));
  }
}
