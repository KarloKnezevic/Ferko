package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TeamSchedulingProblemTest {

  @Test
  void feasibleBalancedSolutionHasZeroPenalty() {
    // 4 equal-weight people, 2 teams, exactly size 2 each, equal totals.
    TeamSchedulingProblem problem = new TeamSchedulingProblem(2, new int[] {3, 3, 3, 3}, 2, 2);
    assertEquals(0.0, problem.penalty(new int[] {0, 1, 0, 1}));
  }

  @Test
  void imbalancedSolutionHasPositivePenalty() {
    TeamSchedulingProblem problem = new TeamSchedulingProblem(2, new int[] {3, 3, 3, 3}, 2, 2);
    // All four in team 0: size violations + weight imbalance.
    assertTrue(problem.penalty(new int[] {0, 0, 0, 0}) > 0.0);
  }

  @Test
  void exactPenaltyOnTinyImbalanceOnlyExample() {
    // weights [1,2,3,4], 2 teams, loose size bounds so only imbalance counts.
    TeamSchedulingProblem problem = new TeamSchedulingProblem(2, new int[] {1, 2, 3, 4}, 0, 4);
    // genes [0,0,1,1] -> team0 weight 3, team1 weight 7, total 10, avg 5.
    // imbalance = (3-5)^2 + (7-5)^2 = 4 + 4 = 8; no size violation.
    assertEquals(8.0, problem.penalty(new int[] {0, 0, 1, 1}));
  }

  @Test
  void exactPenaltyWithSizeViolation() {
    // weights all 0 so imbalance is 0; isolate the size term.
    TeamSchedulingProblem problem = new TeamSchedulingProblem(2, new int[] {0, 0, 0}, 1, 1);
    // genes [0,0,0] -> team0 size 3 (over by 2 -> 4), team1 size 0 (under by 1 -> 1).
    // size penalty = 2^2 + 1^2 = 4 + 1 = 5; imbalance = 0.
    assertEquals(5.0, problem.penalty(new int[] {0, 0, 0}));
  }

  @Test
  void accessorsReflectInputs() {
    TeamSchedulingProblem problem = new TeamSchedulingProblem(3, new int[] {1, 2, 3, 4, 5}, 1, 2);
    assertEquals(5, problem.geneCount());
    assertEquals(5, problem.personCount());
    assertEquals(3, problem.teamCount());
    assertEquals(3, problem.optionCount(0));
  }

  @Test
  void constructorValidatesArguments() {
    assertThrows(
        IllegalArgumentException.class, () -> new TeamSchedulingProblem(0, new int[] {1}, 0, 1));
    assertThrows(IllegalArgumentException.class, () -> new TeamSchedulingProblem(2, null, 0, 1));
    assertThrows(
        IllegalArgumentException.class, () -> new TeamSchedulingProblem(2, new int[] {1}, -1, 1));
    assertThrows(
        IllegalArgumentException.class, () -> new TeamSchedulingProblem(2, new int[] {1}, 3, 2));
    assertThrows(
        IllegalArgumentException.class,
        () -> new TeamSchedulingProblem(2, new int[] {1, -1}, 0, 2));
  }
}
