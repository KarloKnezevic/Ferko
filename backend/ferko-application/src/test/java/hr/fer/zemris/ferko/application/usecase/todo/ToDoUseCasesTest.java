package hr.fer.zemris.ferko.application.usecase.todo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hr.fer.zemris.ferko.application.port.ToDoAuditAction;
import hr.fer.zemris.ferko.application.port.ToDoAuditEvent;
import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import hr.fer.zemris.ferko.application.port.ToDoAuditOutcome;
import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.domain.model.ToDoTask;
import hr.fer.zemris.ferko.domain.model.ToDoTaskPriority;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class ToDoUseCasesTest {

  @Test
  void createUseCaseStoresOpenTask() {
    FakeToDoTaskRepository repository = new FakeToDoTaskRepository();
    FakeAuditLogPort auditLog = new FakeAuditLogPort();
    CreateToDoTaskUseCase useCase = new CreateToDoTaskUseCase(repository, auditLog);

    ToDoTaskView createdTask =
        useCase.execute(
            new CreateToDoTaskCommand(
                10L,
                20L,
                "Prepare migration draft",
                "Collect dependency and architecture notes.",
                LocalDateTime.parse("2026-03-01T09:00:00"),
                "CRITICAL"));

    assertEquals("OPEN", createdTask.status());
    assertEquals(1, repository.tasks.size());
    assertEquals(1, auditLog.events.size());
    assertEquals(ToDoAuditAction.CREATE, auditLog.events.getFirst().action());
    assertEquals(ToDoAuditOutcome.SUCCESS, auditLog.events.getFirst().outcome());
  }

  @Test
  void listMyOpenTasksReturnsOnlyOpenTasksOrderedByDeadline() {
    FakeToDoTaskRepository repository = new FakeToDoTaskRepository();
    repository.save(
        ToDoTask.open(
            repository.nextIdentity(),
            15L,
            30L,
            "Task 2",
            "",
            LocalDateTime.parse("2026-03-10T11:00:00"),
            ToDoTaskPriority.MEDIUM));
    repository.save(
        ToDoTask.open(
                repository.nextIdentity(),
                15L,
                30L,
                "Task 1",
                "",
                LocalDateTime.parse("2026-03-09T11:00:00"),
                ToDoTaskPriority.MEDIUM)
            .close());

    ListMyOpenToDoTasksUseCase useCase = new ListMyOpenToDoTasksUseCase(repository);
    List<ToDoTaskView> tasks = useCase.execute(30L);

    assertEquals(1, tasks.size());
    assertEquals("Task 2", tasks.getFirst().title());
  }

  @Test
  void listAssignedOpenTasksFollowLegacyFilteringAndOrdering() {
    FakeToDoTaskRepository repository = new FakeToDoTaskRepository();
    repository.save(
        ToDoTask.open(
            repository.nextIdentity(),
            40L,
            50L,
            "Assigned to other user",
            "",
            LocalDateTime.parse("2026-03-11T12:00:00"),
            ToDoTaskPriority.MEDIUM));
    repository.save(
        ToDoTask.open(
                repository.nextIdentity(),
                40L,
                45L,
                "Closed task",
                "",
                LocalDateTime.parse("2026-03-10T12:30:00"),
                ToDoTaskPriority.MEDIUM)
            .close());
    repository.save(
        ToDoTask.open(
            repository.nextIdentity(),
            40L,
            51L,
            "Earlier task",
            "",
            LocalDateTime.parse("2026-03-11T10:00:00"),
            ToDoTaskPriority.MEDIUM));
    repository.save(
        ToDoTask.open(
            repository.nextIdentity(),
            40L,
            40L,
            "Self assigned",
            "",
            LocalDateTime.parse("2026-03-11T12:30:00"),
            ToDoTaskPriority.MEDIUM));

    ListAssignedOpenToDoTasksUseCase useCase = new ListAssignedOpenToDoTasksUseCase(repository);
    List<ToDoTaskView> tasks = useCase.execute(40L);

    assertEquals(2, tasks.size());
    assertEquals("Earlier task", tasks.get(0).title());
    assertEquals("Assigned to other user", tasks.get(1).title());
  }

  @Test
  void closeUseCaseRejectsUnauthorizedActor() {
    FakeToDoTaskRepository repository = new FakeToDoTaskRepository();
    FakeAuditLogPort auditLog = new FakeAuditLogPort();
    ToDoTask task =
        ToDoTask.open(
            repository.nextIdentity(),
            60L,
            70L,
            "Security hardening checklist",
            "",
            LocalDateTime.parse("2026-03-15T08:00:00"),
            ToDoTaskPriority.CRITICAL);
    repository.save(task);

    CloseToDoTaskUseCase useCase = new CloseToDoTaskUseCase(repository, auditLog);

    assertThrows(ToDoTaskAccessDeniedException.class, () -> useCase.execute(task.id(), 999L));
    assertEquals(1, auditLog.events.size());
    assertEquals(ToDoAuditAction.CLOSE, auditLog.events.getFirst().action());
    assertEquals(ToDoAuditOutcome.DENIED, auditLog.events.getFirst().outcome());
  }

  @Test
  void closeUseCaseAllowsOwnerAndAssigneeLikeLegacyPolicy() {
    FakeToDoTaskRepository repository = new FakeToDoTaskRepository();
    FakeAuditLogPort auditLog = new FakeAuditLogPort();
    ToDoTask task =
        ToDoTask.open(
            repository.nextIdentity(),
            81L,
            82L,
            "Legacy permission parity",
            "",
            LocalDateTime.parse("2026-03-19T10:00:00"),
            ToDoTaskPriority.MEDIUM);
    repository.save(task);

    CloseToDoTaskUseCase useCase = new CloseToDoTaskUseCase(repository, auditLog);
    useCase.execute(task.id(), 81L);
    repository.save(task);
    useCase.execute(task.id(), 82L);

    assertEquals(2, auditLog.events.size());
    assertEquals(ToDoAuditOutcome.SUCCESS, auditLog.events.get(0).outcome());
    assertEquals(ToDoAuditOutcome.SUCCESS, auditLog.events.get(1).outcome());
  }

  private static final class FakeToDoTaskRepository implements ToDoTaskRepository {
    private final AtomicLong ids = new AtomicLong(0L);
    private final Map<Long, ToDoTask> tasks = new LinkedHashMap<>();

    @Override
    public long nextIdentity() {
      return ids.incrementAndGet();
    }

    @Override
    public void save(ToDoTask task) {
      tasks.put(task.id(), task);
    }

    @Override
    public Optional<ToDoTask> findById(long taskId) {
      return Optional.ofNullable(tasks.get(taskId));
    }

    @Override
    public List<ToDoTask> findByAssigneeId(long assigneeId) {
      List<ToDoTask> result = new ArrayList<>();
      for (ToDoTask task : tasks.values()) {
        if (task.assigneeId() == assigneeId) {
          result.add(task);
        }
      }
      return result;
    }

    @Override
    public List<ToDoTask> findByOwnerId(long ownerId) {
      List<ToDoTask> result = new ArrayList<>();
      for (ToDoTask task : tasks.values()) {
        if (task.ownerId() == ownerId) {
          result.add(task);
        }
      }
      return result;
    }
  }

  private static final class FakeAuditLogPort implements ToDoAuditLogPort {
    private final List<ToDoAuditEvent> events = new ArrayList<>();

    @Override
    public void log(ToDoAuditEvent event) {
      events.add(event);
    }
  }
}
