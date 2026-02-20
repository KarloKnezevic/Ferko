package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.ToDoTask;
import hr.fer.zemris.ferko.domain.model.ToDoTaskPriority;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class InMemoryToDoTaskRepositoryTest {

  @Test
  void savesAndQueriesTasksByOwnerAndAssignee() {
    InMemoryToDoTaskRepository repository = new InMemoryToDoTaskRepository();
    long firstId = repository.nextIdentity();
    long secondId = repository.nextIdentity();

    repository.save(
        ToDoTask.open(
            firstId,
            101L,
            201L,
            "First task",
            "",
            LocalDateTime.parse("2026-03-01T10:00:00"),
            ToDoTaskPriority.TRIVIAL));
    repository.save(
        ToDoTask.open(
            secondId,
            101L,
            202L,
            "Second task",
            "",
            LocalDateTime.parse("2026-03-02T10:00:00"),
            ToDoTaskPriority.MEDIUM));

    assertEquals(2, repository.findByOwnerId(101L).size());
    assertEquals(1, repository.findByAssigneeId(201L).size());
    assertTrue(repository.findById(firstId).isPresent());
  }
}
