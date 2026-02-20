package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hr.fer.zemris.ferko.application.port.ToDoAuditAction;
import hr.fer.zemris.ferko.application.port.ToDoAuditEvent;
import hr.fer.zemris.ferko.application.port.ToDoAuditOutcome;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class JdbcToDoAuditLogRepositoryTest {

  @Test
  void persistsAuditEvent() {
    JdbcTemplate jdbcTemplate = newJdbcTemplateWithSchema();
    JdbcToDoAuditLogRepository repository = new JdbcToDoAuditLogRepository(jdbcTemplate);

    repository.log(
        new ToDoAuditEvent(
            ToDoAuditAction.CLOSE, ToDoAuditOutcome.DENIED, 900L, 12L, "forbidden close attempt"));

    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select action, outcome, actor_user_id, task_id, details from todo_audit_log");
    assertEquals("CLOSE", row.get("action"));
    assertEquals("DENIED", row.get("outcome"));
    assertEquals(900L, ((Number) row.get("actor_user_id")).longValue());
    assertEquals(12L, ((Number) row.get("task_id")).longValue());
    assertEquals("forbidden close attempt", row.get("details"));
  }

  private static JdbcTemplate newJdbcTemplateWithSchema() {
    String databaseName = "todo_audit_" + UUID.randomUUID().toString().replace("-", "");
    String url =
        "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute("create sequence todo_audit_log_seq start with 1 increment by 1");
    jdbcTemplate.execute(
        """
        create table todo_audit_log (
          id bigint default nextval('todo_audit_log_seq') primary key,
          occurred_at timestamp not null default current_timestamp,
          action varchar(32) not null,
          outcome varchar(32) not null,
          actor_user_id bigint,
          task_id bigint,
          details varchar(1000) not null
        )
        """);
    return jdbcTemplate;
  }
}
