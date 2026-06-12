package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.CourseStaff;
import hr.fer.zemris.ferko.domain.model.GroupType;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link CourseRepository} (courses, staff and groups). */
public class JdbcCourseRepository implements CourseRepository {

  private static final RowMapper<Course> COURSE_MAPPER =
      (rs, rowNum) ->
          new Course(
              rs.getLong("id"),
              rs.getString("code"),
              rs.getString("name"),
              rs.getString("semester_code"),
              rs.getInt("ects"),
              rs.getString("description"),
              rs.getString("literature"));

  private static final RowMapper<CourseStaff> STAFF_MAPPER =
      (rs, rowNum) ->
          new CourseStaff(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getLong("user_id"),
              Role.valueOf(rs.getString("role")));

  private static final RowMapper<StudentGroup> GROUP_MAPPER =
      (rs, rowNum) ->
          new StudentGroup(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getString("group_code"),
              GroupType.valueOf(rs.getString("type")),
              rs.getString("category"),
              rs.getInt("capacity"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcCourseRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Course save(Course course) {
    long id = course.id();
    if (id <= 0) {
      id =
          JdbcIds.insert(
              jdbcTemplate,
              "insert into course (code, name, semester_code, ects, description, literature)"
                  + " values (?, ?, ?, ?, ?, ?)",
              course.code(),
              course.name(),
              course.semesterCode(),
              course.ects(),
              course.description(),
              course.literature());
    } else {
      jdbcTemplate.update(
          "update course set code = ?, name = ?, semester_code = ?, ects = ?, description = ?,"
              + " literature = ? where id = ?",
          course.code(),
          course.name(),
          course.semesterCode(),
          course.ects(),
          course.description(),
          course.literature(),
          id);
    }
    return findById(id).orElseThrow();
  }

  @Override
  public Optional<Course> findById(long id) {
    return jdbcTemplate.query("select * from course where id = ?", COURSE_MAPPER, id).stream()
        .findFirst();
  }

  @Override
  public Optional<Course> findByCodeAndSemester(String code, String semesterCode) {
    return jdbcTemplate
        .query(
            "select * from course where code = ? and semester_code = ?",
            COURSE_MAPPER,
            code,
            semesterCode)
        .stream()
        .findFirst();
  }

  @Override
  public List<Course> findBySemester(String semesterCode) {
    return jdbcTemplate.query(
        "select * from course where semester_code = ? order by code", COURSE_MAPPER, semesterCode);
  }

  @Override
  public List<Course> findAll() {
    return jdbcTemplate.query("select * from course order by code", COURSE_MAPPER);
  }

  @Override
  public CourseStaff addStaff(CourseStaff staff) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into course_staff (course_id, user_id, role) values (?, ?, ?)",
            staff.courseId(),
            staff.userId(),
            staff.role().name());
    return new CourseStaff(id, staff.courseId(), staff.userId(), staff.role());
  }

  @Override
  public List<CourseStaff> findStaffByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from course_staff where course_id = ? order by id", STAFF_MAPPER, courseId);
  }

  @Override
  public StudentGroup addGroup(StudentGroup group) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into student_group (course_id, group_code, type, category, capacity)"
                + " values (?, ?, ?, ?, ?)",
            group.courseId(),
            group.groupCode(),
            group.type().name(),
            group.category(),
            group.capacity());
    return new StudentGroup(
        id, group.courseId(), group.groupCode(), group.type(), group.category(), group.capacity());
  }

  @Override
  public List<StudentGroup> findGroupsByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from student_group where course_id = ? order by type, group_code",
        GROUP_MAPPER,
        courseId);
  }
}
