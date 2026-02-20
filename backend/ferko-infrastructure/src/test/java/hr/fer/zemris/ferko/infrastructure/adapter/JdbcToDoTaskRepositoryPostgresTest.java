package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.ToDoTask;
import hr.fer.zemris.ferko.domain.model.ToDoTaskPriority;
import hr.fer.zemris.ferko.domain.model.ToDoTaskStatus;
import java.time.LocalDateTime;
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
class JdbcToDoTaskRepositoryPostgresTest {

  @Container
  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("ferko_todo_test")
          .withUsername("ferko")
          .withPassword("ferko");

  private JdbcToDoTaskRepository repository;

  @BeforeAll
  void setUp() {
    DriverManagerDataSource dataSource =
        new DriverManagerDataSource(
            POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    jdbcTemplate.execute("drop table if exists todo_tasks");
    jdbcTemplate.execute("drop sequence if exists todo_task_seq");
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
    repository = new JdbcToDoTaskRepository(jdbcTemplate);
  }

  @Test
  void repositoryPersistsAndUpdatesToDoTaskOnPostgres() {
    long taskId = repository.nextIdentity();
    ToDoTask task =
        ToDoTask.open(
            taskId,
            5001L,
            6001L,
            "Verify PostgreSQL repository path",
            "Integration test backed by Testcontainers PostgreSQL.",
            LocalDateTime.parse("2026-06-01T09:00:00"),
            ToDoTaskPriority.CRITICAL);

    repository.save(task);

    assertTrue(repository.findById(taskId).isPresent());
    assertEquals(1, repository.findByOwnerId(5001L).size());
    assertEquals(1, repository.findByAssigneeId(6001L).size());

    repository.save(task.close());
    ToDoTask closedTask = repository.findById(taskId).orElseThrow();
    assertEquals(ToDoTaskStatus.CLOSED, closedTask.status());
  }
}
