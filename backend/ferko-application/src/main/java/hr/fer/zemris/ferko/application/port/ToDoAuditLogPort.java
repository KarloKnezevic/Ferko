package hr.fer.zemris.ferko.application.port;

public interface ToDoAuditLogPort {

  void log(ToDoAuditEvent event);
}
