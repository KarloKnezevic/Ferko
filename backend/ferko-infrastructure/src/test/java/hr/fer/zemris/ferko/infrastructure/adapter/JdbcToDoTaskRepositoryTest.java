package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.ToDoTask;
import hr.fer.zemris.ferko.domain.model.ToDoTaskPriority;
import hr.fer.zemris.ferko.domain.model.ToDoTaskStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class JdbcToDoTaskRepositoryTest {

  @Test
  void allocatesIdsAndPersistsTaskLifecycle() {
    JdbcToDoTaskRepository repository = newRepositoryWithSchema();

    long taskId = repository.nextIdentity();
    ToDoTask task =
        ToDoTask.open(
            taskId,
            101L,
            202L,
            "Persist ToDo task",
            "Validate JDBC repository behavior.",
            LocalDateTime.parse("2026-04-01T10:00:00"),
            ToDoTaskPriority.MEDIUM);
    repository.save(task);

    assertTrue(repository.findById(taskId).isPresent());
    assertEquals(1, repository.findByAssigneeId(202L).size());
    assertEquals(1, repository.findByOwnerId(101L).size());

    repository.save(task.close());
    ToDoTask closedTask = repository.findById(taskId).orElseThrow();
    assertEquals(ToDoTaskStatus.CLOSED, closedTask.status());
  }

  private static JdbcToDoTaskRepository newRepositoryWithSchema() {
    String databaseName = "todo_" + UUID.randomUUID().toString().replace("-", "");
    String url =
        "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    jdbcTemplate.execute("create sequence todo_task_seq start with 1 increment by 1");
    jdbcTemplate.execute(
        """
        create table todo_tasks (
          id bigint primary key,
          owner_id bigint not null,
          assignee_id bigint not null,
          title varchar(100) not null,
          description varchar(1000) not null,
          deadline timestamp not null,
          priority varchar(32) not null,
          status varchar(32) not null
        )
        """);

    return new JdbcToDoTaskRepository(jdbcTemplate);
  }
}
