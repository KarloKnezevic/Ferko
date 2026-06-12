package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.GroupMembership;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link EnrollmentRepository} (enrollments and group memberships). */
public class JdbcEnrollmentRepository implements EnrollmentRepository {

  private static final RowMapper<Enrollment> MAPPER =
      (rs, rowNum) ->
          new Enrollment(
              rs.getLong("id"),
              rs.getLong("student_id"),
              rs.getLong("course_id"),
              rs.getTimestamp("enrolled_at").toLocalDateTime(),
              EnrollmentStatus.valueOf(rs.getString("status")));

  private static final RowMapper<GroupMembership> MEMBERSHIP_MAPPER =
      (rs, rowNum) ->
          new GroupMembership(
              rs.getLong("id"), rs.getLong("enrollment_id"), rs.getLong("group_id"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcEnrollmentRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Enrollment save(Enrollment enrollment) {
    long id = enrollment.id();
    if (id <= 0) {
      id =
          JdbcIds.insert(
              jdbcTemplate,
              "insert into enrollment (student_id, course_id, enrolled_at, status)"
                  + " values (?, ?, ?, ?)",
              enrollment.studentId(),
              enrollment.courseId(),
              Timestamp.valueOf(enrollment.enrolledAt()),
              enrollment.status().name());
    } else {
      jdbcTemplate.update(
          "update enrollment set student_id = ?, course_id = ?, enrolled_at = ?, status = ?"
              + " where id = ?",
          enrollment.studentId(),
          enrollment.courseId(),
          Timestamp.valueOf(enrollment.enrolledAt()),
          enrollment.status().name(),
          id);
    }
    return findById(id).orElseThrow();
  }

  @Override
  public Optional<Enrollment> findById(long id) {
    return jdbcTemplate.query("select * from enrollment where id = ?", MAPPER, id).stream()
        .findFirst();
  }

  @Override
  public Optional<Enrollment> findByStudentAndCourse(long studentId, long courseId) {
    return jdbcTemplate
        .query(
            "select * from enrollment where student_id = ? and course_id = ?",
            MAPPER,
            studentId,
            courseId)
        .stream()
        .findFirst();
  }

  @Override
  public List<Enrollment> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from enrollment where course_id = ? order by id", MAPPER, courseId);
  }

  @Override
  public List<Enrollment> findByStudent(long studentId) {
    return jdbcTemplate.query(
        "select * from enrollment where student_id = ? order by id", MAPPER, studentId);
  }

  @Override
  public GroupMembership assignGroup(GroupMembership membership) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into group_membership (enrollment_id, group_id) values (?, ?)",
            membership.enrollmentId(),
            membership.groupId());
    return new GroupMembership(id, membership.enrollmentId(), membership.groupId());
  }

  @Override
  public List<GroupMembership> findMembershipsByEnrollment(long enrollmentId) {
    return jdbcTemplate.query(
        "select * from group_membership where enrollment_id = ? order by id",
        MEMBERSHIP_MAPPER,
        enrollmentId);
  }
}
