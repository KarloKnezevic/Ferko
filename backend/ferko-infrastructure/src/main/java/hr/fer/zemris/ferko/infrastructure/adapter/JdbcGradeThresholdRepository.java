package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.GradeThresholdRepository;
import hr.fer.zemris.ferko.domain.model.CourseGradeThresholds;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link GradeThresholdRepository}. Upserts a single row per course. */
public class JdbcGradeThresholdRepository implements GradeThresholdRepository {

  private static final RowMapper<CourseGradeThresholds> MAPPER =
      (rs, rowNum) ->
          new CourseGradeThresholds(
              rs.getLong("course_id"),
              rs.getInt("excellent"),
              rs.getInt("very_good"),
              rs.getInt("good"),
              rs.getInt("sufficient"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcGradeThresholdRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Optional<CourseGradeThresholds> findByCourse(long courseId) {
    List<CourseGradeThresholds> rows =
        jdbcTemplate.query("select * from grade_threshold where course_id = ?", MAPPER, courseId);
    return rows.stream().findFirst();
  }

  @Override
  public CourseGradeThresholds save(CourseGradeThresholds thresholds) {
    int updated =
        jdbcTemplate.update(
            "update grade_threshold set excellent = ?, very_good = ?, good = ?, sufficient = ?"
                + " where course_id = ?",
            thresholds.excellent(),
            thresholds.veryGood(),
            thresholds.good(),
            thresholds.sufficient(),
            thresholds.courseId());
    if (updated == 0) {
      jdbcTemplate.update(
          "insert into grade_threshold (course_id, excellent, very_good, good, sufficient)"
              + " values (?, ?, ?, ?, ?)",
          thresholds.courseId(),
          thresholds.excellent(),
          thresholds.veryGood(),
          thresholds.good(),
          thresholds.sufficient());
    }
    return thresholds;
  }
}
