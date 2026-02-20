package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.domain.model.ToDoTask;
import hr.fer.zemris.ferko.domain.model.ToDoTaskPriority;
import hr.fer.zemris.ferko.domain.model.ToDoTaskStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class JdbcToDoTaskRepository implements ToDoTaskRepository {

  private static final RowMapper<ToDoTask> TASK_ROW_MAPPER = new ToDoTaskRowMapper();

  private final JdbcTemplate jdbcTemplate;

  public JdbcToDoTaskRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public long nextIdentity() {
    Long id = jdbcTemplate.queryForObject("select nextval('todo_task_seq')", Long.class);
    if (id == null) {
      throw new IllegalStateException("Could not allocate ToDo task id from sequence.");
    }
    return id;
  }

  @Override
  public void save(ToDoTask task) {
    int updatedRows =
        jdbcTemplate.update(
            """
            update todo_tasks
            set owner_id = ?,
                assignee_id = ?,
                title = ?,
                description = ?,
                deadline = ?,
                priority = ?,
                status = ?
            where id = ?
            """,
            task.ownerId(),
            task.assigneeId(),
            task.title(),
            task.description(),
            Timestamp.valueOf(task.deadline()),
            task.priority().name(),
            task.status().name(),
            task.id());

    if (updatedRows == 0) {
      jdbcTemplate.update(
          """
          insert into todo_tasks (
              id,
              owner_id,
              assignee_id,
              title,
              description,
              deadline,
              priority,
              status
          ) values (?, ?, ?, ?, ?, ?, ?, ?)
          """,
          task.id(),
          task.ownerId(),
          task.assigneeId(),
          task.title(),
          task.description(),
          Timestamp.valueOf(task.deadline()),
          task.priority().name(),
          task.status().name());
    }
  }

  @Override
  public Optional<ToDoTask> findById(long taskId) {
    List<ToDoTask> tasks =
        jdbcTemplate.query("select * from todo_tasks where id = ?", TASK_ROW_MAPPER, taskId);
    return tasks.stream().findFirst();
  }

  @Override
  public List<ToDoTask> findByAssigneeId(long assigneeId) {
    return jdbcTemplate.query(
        "select * from todo_tasks where assignee_id = ? order by deadline, id",
        TASK_ROW_MAPPER,
        assigneeId);
  }

  @Override
  public List<ToDoTask> findByOwnerId(long ownerId) {
    return jdbcTemplate.query(
        "select * from todo_tasks where owner_id = ? order by deadline, id",
        TASK_ROW_MAPPER,
        ownerId);
  }

  private static final class ToDoTaskRowMapper implements RowMapper<ToDoTask> {

    @Override
    public ToDoTask mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new ToDoTask(
          rs.getLong("id"),
          rs.getLong("owner_id"),
          rs.getLong("assignee_id"),
          rs.getString("title"),
          rs.getString("description"),
          rs.getTimestamp("deadline").toLocalDateTime(),
          ToDoTaskPriority.valueOf(rs.getString("priority")),
          ToDoTaskStatus.valueOf(rs.getString("status")));
    }
  }
}
