package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hr.fer.zemris.ferko.application.port.ToDoAuditAction;
import hr.fer.zemris.ferko.application.port.ToDoAuditEvent;
import hr.fer.zemris.ferko.application.port.ToDoAuditOutcome;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JdbcToDoAuditLogRepositoryPostgresTest {

  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("ferko_todo_audit_test")
          .withUsername("ferko")
          .withPassword("ferko");

  private JdbcTemplate jdbcTemplate;
  private JdbcToDoAuditLogRepository repository;

  @BeforeAll
  void setUp() {
    DriverManagerDataSource dataSource =
        new DriverManagerDataSource(
            POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute("drop table if exists todo_audit_log");
    jdbcTemplate.execute("drop sequence if exists todo_audit_log_seq");
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
    repository = new JdbcToDoAuditLogRepository(jdbcTemplate);
  }

  @Test
  void persistsAuditEventOnPostgres() {
    repository.log(
        new ToDoAuditEvent(
            ToDoAuditAction.CREATE, ToDoAuditOutcome.SUCCESS, 51L, 1L, "task created"));

    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select action, outcome, actor_user_id, task_id from todo_audit_log limit 1");
    assertEquals("CREATE", row.get("action"));
    assertEquals("SUCCESS", row.get("outcome"));
    assertEquals(51L, ((Number) row.get("actor_user_id")).longValue());
    assertEquals(1L, ((Number) row.get("task_id")).longValue());
  }
}
