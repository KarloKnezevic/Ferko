package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.CourseLiteratureRepository;
import hr.fer.zemris.ferko.domain.model.CourseLiterature;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link CourseLiteratureRepository}. */
public class JdbcCourseLiteratureRepository implements CourseLiteratureRepository {

  private static final RowMapper<CourseLiterature> MAPPER =
      (rs, rowNum) ->
          new CourseLiterature(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getString("title"),
              rs.getString("author"),
              rs.getBoolean("mandatory"),
              rs.getInt("ordinal"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcCourseLiteratureRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public CourseLiterature save(CourseLiterature literature) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into literatura_kolegija (course_id, title, author, mandatory, ordinal)"
                + " values (?, ?, ?, ?, ?)",
            literature.courseId(),
            literature.title(),
            literature.author(),
            literature.mandatory(),
            literature.ordinal());
    return new CourseLiterature(
        id,
        literature.courseId(),
        literature.title(),
        literature.author(),
        literature.mandatory(),
        literature.ordinal());
  }

  @Override
  public List<CourseLiterature> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from literatura_kolegija where course_id = ? order by ordinal, id",
        MAPPER,
        courseId);
  }
}
