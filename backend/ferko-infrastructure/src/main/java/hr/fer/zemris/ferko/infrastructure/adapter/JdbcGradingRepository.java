package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.GradingRepository;
import hr.fer.zemris.ferko.domain.model.Grade;
import hr.fer.zemris.ferko.domain.model.GradeComponent;
import hr.fer.zemris.ferko.domain.model.StudentPoints;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link GradingRepository}. */
public class JdbcGradingRepository implements GradingRepository {

  private static final RowMapper<GradeComponent> COMPONENT_MAPPER =
      (rs, rowNum) ->
          new GradeComponent(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getString("name"),
              rs.getString("short_name"),
              rs.getDouble("max_points"),
              rs.getInt("ordinal"));

  private static final RowMapper<StudentPoints> POINTS_MAPPER =
      (rs, rowNum) -> {
        Number componentId = (Number) rs.getObject("component_id");
        Number examId = (Number) rs.getObject("exam_id");
        Timestamp enteredAt = rs.getTimestamp("entered_at");
        return new StudentPoints(
            rs.getLong("id"),
            rs.getLong("course_id"),
            rs.getLong("student_id"),
            componentId == null ? null : componentId.longValue(),
            examId == null ? null : examId.longValue(),
            rs.getDouble("points"),
            rs.getDouble("max_points"),
            rs.getBoolean("published"),
            rs.getString("entered_by"),
            enteredAt == null ? null : enteredAt.toLocalDateTime());
      };

  private static final RowMapper<Grade> GRADE_MAPPER =
      (rs, rowNum) ->
          new Grade(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getLong("student_id"),
              rs.getInt("final_grade"),
              rs.getDouble("points_total"),
              rs.getString("decided_by"),
              rs.getTimestamp("decided_at").toLocalDateTime());

  private final JdbcTemplate jdbcTemplate;

  public JdbcGradingRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public GradeComponent addComponent(GradeComponent component) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into grade_component (course_id, name, short_name, max_points, ordinal)"
                + " values (?, ?, ?, ?, ?)",
            component.courseId(),
            component.name(),
            component.shortName(),
            component.maxPoints(),
            component.ordinal());
    return new GradeComponent(
        id,
        component.courseId(),
        component.name(),
        component.shortName(),
        component.maxPoints(),
        component.ordinal());
  }

  @Override
  public List<GradeComponent> componentsByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from grade_component where course_id = ? order by ordinal, id",
        COMPONENT_MAPPER,
        courseId);
  }

  @Override
  public StudentPoints savePoints(StudentPoints points) {
    Timestamp enteredAt = points.enteredAt() == null ? null : Timestamp.valueOf(points.enteredAt());
    int updated =
        jdbcTemplate.update(
            "update student_points set points = ?, max_points = ?, published = ?, entered_by = ?,"
                + " entered_at = ? where course_id = ? and student_id = ? and component_id = ?",
            points.points(),
            points.maxPoints(),
            points.published(),
            points.enteredBy(),
            enteredAt,
            points.courseId(),
            points.studentId(),
            points.componentId());
    if (updated == 0) {
      jdbcTemplate.update(
          "insert into student_points (course_id, student_id, component_id, exam_id, points,"
              + " max_points, published, entered_by, entered_at)"
              + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
          points.courseId(),
          points.studentId(),
          points.componentId(),
          points.examId(),
          points.points(),
          points.maxPoints(),
          points.published(),
          points.enteredBy(),
          enteredAt);
    }
    return points;
  }

  @Override
  public List<StudentPoints> pointsByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from student_points where course_id = ? order by student_id, id",
        POINTS_MAPPER,
        courseId);
  }

  @Override
  public Grade saveGrade(Grade grade) {
    Timestamp decidedAt = grade.decidedAt() == null ? null : Timestamp.valueOf(grade.decidedAt());
    int updated =
        jdbcTemplate.update(
            "update grade set final_grade = ?, points_total = ?, decided_by = ?, decided_at = ?"
                + " where course_id = ? and student_id = ?",
            grade.finalGrade(),
            grade.pointsTotal(),
            grade.decidedBy(),
            decidedAt,
            grade.courseId(),
            grade.studentId());
    if (updated == 0) {
      jdbcTemplate.update(
          "insert into grade (course_id, student_id, final_grade, points_total, decided_by,"
              + " decided_at) values (?, ?, ?, ?, ?, ?)",
          grade.courseId(),
          grade.studentId(),
          grade.finalGrade(),
          grade.pointsTotal(),
          grade.decidedBy(),
          decidedAt);
    }
    return grade;
  }

  @Override
  public List<Grade> gradesByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from grade where course_id = ? order by student_id", GRADE_MAPPER, courseId);
  }

  @Override
  public Optional<Grade> gradeFor(long courseId, long studentId) {
    return jdbcTemplate
        .query(
            "select * from grade where course_id = ? and student_id = ?",
            GRADE_MAPPER,
            courseId,
            studentId)
        .stream()
        .findFirst();
  }
}
