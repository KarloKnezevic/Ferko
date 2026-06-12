package hr.fer.zemris.ferko.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SeatingStrategiesTest {

  private static final int[] ROOMS = {10, 8, 6};

  @Test
  void everyStrategyAssignsAllStudentsWithoutOverflow() {
    int students = 20;
    assertValid(SeatingStrategies.sortedGreedy(students, ROOMS), students);
    assertValid(SeatingStrategies.sortedProportional(students, ROOMS), students);
    assertValid(SeatingStrategies.randomGreedy(students, ROOMS, 1L), students);
    assertValid(SeatingStrategies.randomProportional(students, ROOMS, 1L), students);
  }

  @Test
  void greedyFillsRoomsSequentially() {
    int[] assignment = SeatingStrategies.sortedGreedy(15, ROOMS);
    // First 10 students in room 0, next 5 in room 1.
    for (int i = 0; i < 10; i++) {
      assertEquals(0, assignment[i]);
    }
    for (int i = 10; i < 15; i++) {
      assertEquals(1, assignment[i]);
    }
  }

  @Test
  void proportionalBalancesAcrossRooms() {
    int[] assignment = SeatingStrategies.sortedProportional(12, new int[] {6, 6});
    int room0 = 0;
    int room1 = 0;
    for (int room : assignment) {
      if (room == 0) {
        room0++;
      } else {
        room1++;
      }
    }
    assertEquals(6, room0);
    assertEquals(6, room1);
  }

  @Test
  void randomStrategyIsDeterministicForSeed() {
    int[] first = SeatingStrategies.randomGreedy(18, ROOMS, 99L);
    int[] second = SeatingStrategies.randomGreedy(18, ROOMS, 99L);
    org.junit.jupiter.api.Assertions.assertArrayEquals(first, second);
  }

  private static void assertValid(int[] assignment, int students) {
    assertEquals(students, assignment.length);
    SeatingProblem problem = new SeatingProblem(students, ROOMS, 2.0);
    assertEquals(0.0, problem.penalty(assignment), "strategy must not overflow rooms");
    for (int room : assignment) {
      assertTrue(room >= 0 && room < ROOMS.length);
    }
  }
}
