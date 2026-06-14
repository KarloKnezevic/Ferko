package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.DemonstratorRepository;
import hr.fer.zemris.ferko.domain.model.Demonstrator;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link DemonstratorRepository}. */
public class JdbcDemonstratorRepository implements DemonstratorRepository {

  private static final RowMapper<Demonstrator> MAPPER =
      (rs, rowNum) ->
          new Demonstrator(rs.getLong("id"), rs.getLong("course_id"), rs.getLong("student_id"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcDemonstratorRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Demonstrator save(Demonstrator demonstrator) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into demonstrator (course_id, student_id) values (?, ?)",
            demonstrator.courseId(),
            demonstrator.studentId());
    return new Demonstrator(id, demonstrator.courseId(), demonstrator.studentId());
  }

  @Override
  public List<Demonstrator> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from demonstrator where course_id = ? order by student_id", MAPPER, courseId);
  }

  @Override
  public List<Demonstrator> findByStudent(long studentId) {
    return jdbcTemplate.query(
        "select * from demonstrator where student_id = ? order by course_id", MAPPER, studentId);
  }

  @Override
  public boolean exists(long courseId, long studentId) {
    Integer count =
        jdbcTemplate.queryForObject(
            "select count(*) from demonstrator where course_id = ? and student_id = ?",
            Integer.class,
            courseId,
            studentId);
    return count != null && count > 0;
  }

  @Override
  public boolean delete(long courseId, long studentId) {
    return jdbcTemplate.update(
            "delete from demonstrator where course_id = ? and student_id = ?", courseId, studentId)
        > 0;
  }
}
