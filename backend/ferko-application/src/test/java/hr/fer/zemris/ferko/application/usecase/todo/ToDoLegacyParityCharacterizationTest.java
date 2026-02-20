package hr.fer.zemris.ferko.application.usecase.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hr.fer.zemris.ferko.application.port.ToDoAuditEvent;
import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.domain.model.ToDoTask;
import hr.fer.zemris.ferko.domain.model.ToDoTaskPriority;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class ToDoLegacyParityCharacterizationTest {

  @Test
  void assignedListParity_excludesSelfAssignedAndClosed_ordersByDeadline() {
    FakeToDoTaskRepository repository = new FakeToDoTaskRepository();
    repository.save(
        ToDoTask.open(
            repository.nextIdentity(),
            15L,
            16L,
            "later open",
            "",
            LocalDateTime.parse("2026-06-20T10:00:00"),
            ToDoTaskPriority.MEDIUM));
    repository.save(
        ToDoTask.open(
                repository.nextIdentity(),
                15L,
                16L,
                "closed",
                "",
                LocalDateTime.parse("2026-06-19T10:00:00"),
                ToDoTaskPriority.MEDIUM)
            .close());
    repository.save(
        ToDoTask.open(
            repository.nextIdentity(),
            15L,
            15L,
            "self assigned",
            "",
            LocalDateTime.parse("2026-06-18T10:00:00"),
            ToDoTaskPriority.MEDIUM));
    repository.save(
        ToDoTask.open(
            repository.nextIdentity(),
            15L,
            17L,
            "earlier open",
            "",
            LocalDateTime.parse("2026-06-18T11:00:00"),
            ToDoTaskPriority.MEDIUM));

    ListAssignedOpenToDoTasksUseCase useCase = new ListAssignedOpenToDoTasksUseCase(repository);
    List<ToDoTaskView> result = useCase.execute(15L);

    assertEquals(2, result.size());
    assertEquals("earlier open", result.get(0).title());
    assertEquals("later open", result.get(1).title());
  }

  @Test
  void managePermissionParity_ownerAndAssigneeCanClose_unrelatedCannot() {
    FakeToDoTaskRepository repository = new FakeToDoTaskRepository();
    NoOpAuditLog auditLog = new NoOpAuditLog();
    ToDoTask task =
        ToDoTask.open(
            repository.nextIdentity(),
            100L,
            200L,
            "legacy permission parity",
            "",
            LocalDateTime.parse("2026-06-25T10:00:00"),
            ToDoTaskPriority.CRITICAL);
    repository.save(task);

    CloseToDoTaskUseCase useCase = new CloseToDoTaskUseCase(repository, auditLog);
    assertEquals("CLOSED", useCase.execute(task.id(), 100L).status());

    repository.save(task);
    assertEquals("CLOSED", useCase.execute(task.id(), 200L).status());

    repository.save(task);
    assertThrows(ToDoTaskAccessDeniedException.class, () -> useCase.execute(task.id(), 300L));
  }

  private static final class NoOpAuditLog implements ToDoAuditLogPort {
    @Override
    public void log(ToDoAuditEvent event) {}
  }

  private static final class FakeToDoTaskRepository implements ToDoTaskRepository {
    private final AtomicLong ids = new AtomicLong(0L);
    private final List<ToDoTask> tasks = new ArrayList<>();

    @Override
    public long nextIdentity() {
      return ids.incrementAndGet();
    }

    @Override
    public void save(ToDoTask task) {
      for (int i = 0; i < tasks.size(); i++) {
        if (tasks.get(i).id() == task.id()) {
          tasks.set(i, task);
          return;
        }
      }
      tasks.add(task);
    }

    @Override
    public Optional<ToDoTask> findById(long taskId) {
      return tasks.stream().filter(task -> task.id() == taskId).findFirst();
    }

    @Override
    public List<ToDoTask> findByAssigneeId(long assigneeId) {
      return tasks.stream().filter(task -> task.assigneeId() == assigneeId).toList();
    }

    @Override
    public List<ToDoTask> findByOwnerId(long ownerId) {
      return tasks.stream().filter(task -> task.ownerId() == ownerId).toList();
    }
  }
}
