package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.GroupType;
import java.time.DayOfWeek;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link ClassScheduleRepository}. */
public class JdbcClassScheduleRepository implements ClassScheduleRepository {

  private static final RowMapper<ClassSchedule> MAPPER =
      (rs, rowNum) ->
          new ClassSchedule(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getObject("group_id") == null ? null : rs.getLong("group_id"),
              GroupType.valueOf(rs.getString("type")),
              rs.getObject("room_id") == null ? null : rs.getLong("room_id"),
              DayOfWeek.valueOf(rs.getString("day_of_week")),
              rs.getTime("starts_at").toLocalTime(),
              rs.getTime("ends_at").toLocalTime(),
              rs.getString("instructor"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcClassScheduleRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public ClassSchedule save(ClassSchedule entry) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into class_schedule"
                + " (course_id, group_id, type, room_id, day_of_week, starts_at, ends_at,"
                + " instructor) values (?, ?, ?, ?, ?, ?, ?, ?)",
            entry.courseId(),
            entry.groupId(),
            entry.type().name(),
            entry.roomId(),
            entry.dayOfWeek().name(),
            java.sql.Time.valueOf(entry.startsAt()),
            java.sql.Time.valueOf(entry.endsAt()),
            entry.instructor());
    return new ClassSchedule(
        id,
        entry.courseId(),
        entry.groupId(),
        entry.type(),
        entry.roomId(),
        entry.dayOfWeek(),
        entry.startsAt(),
        entry.endsAt(),
        entry.instructor());
  }

  @Override
  public List<ClassSchedule> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from class_schedule where course_id = ? order by day_of_week, starts_at",
        MAPPER,
        courseId);
  }

  @Override
  public List<ClassSchedule> findAll() {
    return jdbcTemplate.query(
        "select * from class_schedule order by day_of_week, starts_at", MAPPER);
  }
}
