package hr.fer.zemris.ferko.webapi.config;

import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.application.usecase.PingUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.CloseToDoTaskUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.CreateToDoTaskUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.ListAssignedOpenToDoTasksUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.ListMyOpenToDoTasksUseCase;
import hr.fer.zemris.ferko.infrastructure.adapter.InMemoryAuditAdapter;
import hr.fer.zemris.ferko.infrastructure.adapter.InMemoryToDoTaskRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcToDoAuditLogRepository;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcToDoTaskRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ApplicationBeans {

  @Bean
  public PingUseCase pingUseCase() {
    return new PingUseCase();
  }

  @Bean
  @ConditionalOnProperty(name = "ferko.todo.repository", havingValue = "in-memory")
  public ToDoTaskRepository inMemoryToDoTaskRepository() {
    return new InMemoryToDoTaskRepository();
  }

  @Bean
  @ConditionalOnProperty(
      name = "ferko.todo.repository",
      havingValue = "jdbc",
      matchIfMissing = true)
  public ToDoTaskRepository jdbcToDoTaskRepository(JdbcTemplate jdbcTemplate) {
    return new JdbcToDoTaskRepository(jdbcTemplate);
  }

  @Bean
  @ConditionalOnProperty(name = "ferko.audit.repository", havingValue = "in-memory")
  public ToDoAuditLogPort inMemoryToDoAuditLogPort() {
    return new InMemoryAuditAdapter();
  }

  @Bean
  @ConditionalOnProperty(
      name = "ferko.audit.repository",
      havingValue = "jdbc",
      matchIfMissing = true)
  public ToDoAuditLogPort jdbcToDoAuditLogPort(JdbcTemplate jdbcTemplate) {
    return new JdbcToDoAuditLogRepository(jdbcTemplate);
  }

  @Bean
  public CreateToDoTaskUseCase createToDoTaskUseCase(
      ToDoTaskRepository repository, ToDoAuditLogPort auditLogPort) {
    return new CreateToDoTaskUseCase(repository, auditLogPort);
  }

  @Bean
  public ListMyOpenToDoTasksUseCase listMyOpenToDoTasksUseCase(ToDoTaskRepository repository) {
    return new ListMyOpenToDoTasksUseCase(repository);
  }

  @Bean
  public ListAssignedOpenToDoTasksUseCase listAssignedOpenToDoTasksUseCase(
      ToDoTaskRepository repository) {
    return new ListAssignedOpenToDoTasksUseCase(repository);
  }

  @Bean
  public CloseToDoTaskUseCase closeToDoTaskUseCase(
      ToDoTaskRepository repository, ToDoAuditLogPort auditLogPort) {
    return new CloseToDoTaskUseCase(repository, auditLogPort);
  }
}
