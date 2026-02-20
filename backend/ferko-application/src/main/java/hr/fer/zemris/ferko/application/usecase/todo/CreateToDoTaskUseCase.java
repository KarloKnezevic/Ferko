package hr.fer.zemris.ferko.application.usecase.todo;

import hr.fer.zemris.ferko.application.port.ToDoAuditAction;
import hr.fer.zemris.ferko.application.port.ToDoAuditEvent;
import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import hr.fer.zemris.ferko.application.port.ToDoAuditOutcome;
import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.domain.model.ToDoTask;
import hr.fer.zemris.ferko.domain.model.ToDoTaskPriority;
import java.util.Locale;

public class CreateToDoTaskUseCase {

  private final ToDoTaskRepository repository;
  private final ToDoAuditLogPort auditLogPort;

  public CreateToDoTaskUseCase(ToDoTaskRepository repository, ToDoAuditLogPort auditLogPort) {
    this.repository = repository;
    this.auditLogPort = auditLogPort;
  }

  public ToDoTaskView execute(CreateToDoTaskCommand command) {
    if (command.priority() == null || command.priority().isBlank()) {
      throw new IllegalArgumentException("Priority must not be blank.");
    }

    long taskId = repository.nextIdentity();
    ToDoTask task =
        ToDoTask.open(
            taskId,
            command.ownerId(),
            command.assigneeId(),
            command.title(),
            command.description(),
            command.deadline(),
            ToDoTaskPriority.valueOf(command.priority().trim().toUpperCase(Locale.ROOT)));
    repository.save(task);
    auditLogPort.log(
        new ToDoAuditEvent(
            ToDoAuditAction.CREATE,
            ToDoAuditOutcome.SUCCESS,
            command.ownerId(),
            task.id(),
            "ToDo task created."));
    return ToDoTaskViewMapper.toView(task);
  }
}
