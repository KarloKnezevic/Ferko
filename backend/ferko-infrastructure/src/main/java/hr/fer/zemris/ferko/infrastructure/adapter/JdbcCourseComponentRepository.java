package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.CourseComponentRepository;
import hr.fer.zemris.ferko.domain.model.CourseComponent;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link CourseComponentRepository}. */
public class JdbcCourseComponentRepository implements CourseComponentRepository {

  private static final RowMapper<CourseComponent> MAPPER =
      (rs, rowNum) ->
          new CourseComponent(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getString("title"),
              rs.getString("content"),
              rs.getInt("ordinal"),
              rs.getBoolean("visible"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcCourseComponentRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public CourseComponent save(CourseComponent component) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into komponenta_kolegija (course_id, title, content, ordinal, visible)"
                + " values (?, ?, ?, ?, ?)",
            component.courseId(),
            component.title(),
            component.content(),
            component.ordinal(),
            component.visible());
    return new CourseComponent(
        id,
        component.courseId(),
        component.title(),
        component.content(),
        component.ordinal(),
        component.visible());
  }

  @Override
  public List<CourseComponent> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from komponenta_kolegija where course_id = ? order by ordinal, id",
        MAPPER,
        courseId);
  }
}
