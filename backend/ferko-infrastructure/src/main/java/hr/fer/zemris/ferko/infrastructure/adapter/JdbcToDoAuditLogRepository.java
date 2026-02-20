package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.ToDoAuditEvent;
import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcToDoAuditLogRepository implements ToDoAuditLogPort {

  private final JdbcTemplate jdbcTemplate;

  public JdbcToDoAuditLogRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void log(ToDoAuditEvent event) {
    jdbcTemplate.update(
        """
        insert into todo_audit_log (
            action,
            outcome,
            actor_user_id,
            task_id,
            details
        ) values (?, ?, ?, ?, ?)
        """,
        event.action().name(),
        event.outcome().name(),
        event.actorUserId(),
        event.taskId(),
        event.details());
  }
}
