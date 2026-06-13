package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LabSchedulingProblemTest {

  @Test
  void feasibleSolutionHasZeroPenalty() {
    // 3 events (4, 4, 4 students), 2 slots each with capacity 8, resource 0 with quantity 2.
    LabSchedulingProblem problem =
        new LabSchedulingProblem(
            2, new int[] {4, 4, 4}, new int[] {8, 8}, new int[] {0, 0, -1}, new int[] {2});
    // Slot 0: events 0+1 = 8 students (<= 8), resource 0 used twice (<= 2).
    // Slot 1: event 2 = 4 students (<= 8), no resource.
    assertEquals(0.0, problem.penalty(new int[] {0, 0, 1}));
  }

  @Test
  void capacityOverflowProducesPositivePenalty() {
    LabSchedulingProblem problem =
        new LabSchedulingProblem(
            2, new int[] {4, 4, 4}, new int[] {8, 8}, new int[] {-1, -1, -1}, new int[0]);
    // All three events (12 students) in slot 0 over capacity 8 -> overflow 4 -> 16.
    assertTrue(problem.penalty(new int[] {0, 0, 0}) > 0.0);
  }

  @Test
  void exactPenaltyOnTinyExample() {
    LabSchedulingProblem problem =
        new LabSchedulingProblem(
            1, new int[] {5, 5, 5}, new int[] {10}, new int[] {0, 0, 0}, new int[] {1});
    // Single slot: load 15 - capacity 10 = overflow 5 -> 25.
    // Resource 0 used 3 times - quantity 1 = overuse 2 -> 4.
    // Total = 29.
    assertEquals(29.0, problem.penalty(new int[] {0, 0, 0}));
  }

  @Test
  void resourceOveruseAloneProducesPenalty() {
    LabSchedulingProblem problem =
        new LabSchedulingProblem(
            1, new int[] {1, 1, 1}, new int[] {100}, new int[] {0, 0, 0}, new int[] {1});
    // Capacity fine (3 <= 100); resource used 3 - 1 = 2 -> 4.
    assertEquals(4.0, problem.penalty(new int[] {0, 0, 0}));
  }

  @Test
  void accessorsReportSizes() {
    LabSchedulingProblem problem =
        new LabSchedulingProblem(
            3, new int[] {1, 2}, new int[] {5, 5, 5}, new int[] {-1, 0}, new int[] {4, 4});
    assertEquals(2, problem.eventCount());
    assertEquals(2, problem.geneCount());
    assertEquals(3, problem.timeSlotCount());
    assertEquals(3, problem.optionCount(0));
    assertEquals(2, problem.resourceCount());
  }

  @Test
  void constructorValidatesArguments() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LabSchedulingProblem(0, new int[] {1}, new int[] {1}, new int[] {-1}, new int[0]));
    assertThrows(
        IllegalArgumentException.class,
        () -> new LabSchedulingProblem(2, null, new int[] {1, 1}, new int[0], new int[0]));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LabSchedulingProblem(
                2, new int[] {1}, new int[] {1}, new int[] {-1}, new int[0])); // slot cap length
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LabSchedulingProblem(
                1, new int[] {1}, new int[] {1}, new int[] {-1, -1}, new int[0])); // resource len
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LabSchedulingProblem(
                1, new int[] {-1}, new int[] {1}, new int[] {-1}, new int[0])); // negative students
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LabSchedulingProblem(
                1, new int[] {1}, new int[] {-1}, new int[] {-1}, new int[0])); // negative capacity
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LabSchedulingProblem(
                1, new int[] {1}, new int[] {1}, new int[] {-1}, new int[] {-1})); // neg quantity
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new LabSchedulingProblem(
                1, new int[] {1}, new int[] {1}, new int[] {5}, new int[] {1})); // resource oob
  }

  @Test
  void penaltyValidatesGenes() {
    LabSchedulingProblem problem =
        new LabSchedulingProblem(2, new int[] {1}, new int[] {1, 1}, new int[] {-1}, new int[0]);
    assertThrows(IllegalArgumentException.class, () -> problem.penalty(null));
    assertThrows(IllegalArgumentException.class, () -> problem.penalty(new int[] {0, 0}));
    assertThrows(IllegalArgumentException.class, () -> problem.penalty(new int[] {2}));
  }
}
