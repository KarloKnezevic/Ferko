package hr.fer.zemris.ferko.application.port;

public record ToDoAuditEvent(
    ToDoAuditAction action,
    ToDoAuditOutcome outcome,
    Long actorUserId,
    Long taskId,
    String details) {

  public ToDoAuditEvent {
    if (action == null) {
      throw new IllegalArgumentException("Audit action must not be null.");
    }
    if (outcome == null) {
      throw new IllegalArgumentException("Audit outcome must not be null.");
    }
    details = details == null ? "" : details.trim();
  }
}
