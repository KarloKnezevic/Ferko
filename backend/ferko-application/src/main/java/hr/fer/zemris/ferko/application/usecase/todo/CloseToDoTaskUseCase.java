package hr.fer.zemris.ferko.application.usecase.todo;

import hr.fer.zemris.ferko.application.port.ToDoAuditAction;
import hr.fer.zemris.ferko.application.port.ToDoAuditEvent;
import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import hr.fer.zemris.ferko.application.port.ToDoAuditOutcome;
import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.domain.model.ToDoTask;

public class CloseToDoTaskUseCase {

  private final ToDoTaskRepository repository;
  private final ToDoAuditLogPort auditLogPort;

  public CloseToDoTaskUseCase(ToDoTaskRepository repository, ToDoAuditLogPort auditLogPort) {
    this.repository = repository;
    this.auditLogPort = auditLogPort;
  }

  public ToDoTaskView execute(long taskId, long actorUserId) {
    ToDoTask task =
        repository.findById(taskId).orElseThrow(() -> new ToDoTaskNotFoundException(taskId));
    if (!task.isManagedBy(actorUserId)) {
      auditLogPort.log(
          new ToDoAuditEvent(
              ToDoAuditAction.CLOSE,
              ToDoAuditOutcome.DENIED,
              actorUserId,
              taskId,
              "Actor cannot manage task."));
      throw new ToDoTaskAccessDeniedException(taskId, actorUserId);
    }

    ToDoTask closedTask = task.close();
    repository.save(closedTask);
    auditLogPort.log(
        new ToDoAuditEvent(
            ToDoAuditAction.CLOSE,
            ToDoAuditOutcome.SUCCESS,
            actorUserId,
            taskId,
            "ToDo task closed."));
    return ToDoTaskViewMapper.toView(closedTask);
  }
}
