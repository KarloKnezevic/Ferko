package hr.fer.zemris.ferko.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class ToDoTaskTest {

  @Test
  void rejectsBlankTitle() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            ToDoTask.open(
                1L,
                100L,
                200L,
                " ",
                "description",
                LocalDateTime.parse("2026-03-01T10:00:00"),
                ToDoTaskPriority.MEDIUM));
  }

  @Test
  void closesOpenTask() {
    ToDoTask openTask =
        ToDoTask.open(
            5L,
            100L,
            200L,
            "Prepare migration task",
            "Document and execute migration.",
            LocalDateTime.parse("2026-03-10T09:30:00"),
            ToDoTaskPriority.CRITICAL);

    ToDoTask closedTask = openTask.close();

    assertEquals(ToDoTaskStatus.CLOSED, closedTask.status());
  }

  @Test
  void managedByReturnsTrueForOwnerAndAssignee() {
    ToDoTask task =
        ToDoTask.open(
            7L,
            300L,
            400L,
            "Collect architecture metrics",
            "",
            LocalDateTime.parse("2026-04-01T14:00:00"),
            ToDoTaskPriority.TRIVIAL);

    assertTrue(task.isManagedBy(300L));
    assertTrue(task.isManagedBy(400L));
  }
}
