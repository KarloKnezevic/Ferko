package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.ExamRepository;
import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamKind;
import hr.fer.zemris.ferko.domain.model.ExamRegistration;
import hr.fer.zemris.ferko.domain.model.ExamRoom;
import hr.fer.zemris.ferko.domain.model.ExamSeat;
import hr.fer.zemris.ferko.domain.model.ExamVisibility;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link ExamRepository} (exams, registrations, rooms and seating). */
public class JdbcExamRepository implements ExamRepository {

  private static final RowMapper<Exam> EXAM_MAPPER = JdbcExamRepository::mapExam;

  private static final RowMapper<ExamRegistration> REGISTRATION_MAPPER =
      (rs, rowNum) ->
          new ExamRegistration(
              rs.getLong("id"),
              rs.getLong("exam_id"),
              rs.getLong("student_id"),
              rs.getTimestamp("registered_at").toLocalDateTime(),
              rs.getString("status"));

  private static final RowMapper<ExamRoom> ROOM_MAPPER =
      (rs, rowNum) ->
          new ExamRoom(
              rs.getLong("id"),
              rs.getLong("exam_id"),
              rs.getLong("room_id"),
              rs.getInt("capacity"),
              rs.getInt("required_assistants"),
              rs.getBoolean("reserved"));

  private static final RowMapper<ExamSeat> SEAT_MAPPER =
      (rs, rowNum) -> {
        Number seatNo = (Number) rs.getObject("seat_no");
        return new ExamSeat(
            rs.getLong("id"),
            rs.getLong("exam_id"),
            rs.getLong("student_id"),
            rs.getLong("room_id"),
            seatNo == null ? null : seatNo.intValue(),
            rs.getString("test_group"));
      };

  private final JdbcTemplate jdbcTemplate;

  public JdbcExamRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Exam save(Exam exam) {
    Timestamp startsAt = exam.startsAt() == null ? null : Timestamp.valueOf(exam.startsAt());
    long id = exam.id();
    if (id <= 0) {
      id =
          JdbcIds.insert(
              jdbcTemplate,
              "insert into exam (course_id, title, short_name, kind, starts_at, duration_minutes,"
                  + " max_points, ordinal, visibility, locked, prerequisite_flag_id, published)"
                  + " values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
              exam.courseId(),
              exam.title(),
              exam.shortName(),
              exam.kind().name(),
              startsAt,
              exam.durationMinutes(),
              exam.maxPoints(),
              exam.ordinal(),
              exam.visibility().name(),
              exam.locked(),
              exam.prerequisiteFlagId(),
              exam.published());
    } else {
      jdbcTemplate.update(
          "update exam set course_id = ?, title = ?, short_name = ?, kind = ?, starts_at = ?,"
              + " duration_minutes = ?, max_points = ?, ordinal = ?, visibility = ?, locked = ?,"
              + " prerequisite_flag_id = ?, published = ? where id = ?",
          exam.courseId(),
          exam.title(),
          exam.shortName(),
          exam.kind().name(),
          startsAt,
          exam.durationMinutes(),
          exam.maxPoints(),
          exam.ordinal(),
          exam.visibility().name(),
          exam.locked(),
          exam.prerequisiteFlagId(),
          exam.published(),
          id);
    }
    return findById(id).orElseThrow();
  }

  @Override
  public Optional<Exam> findById(long id) {
    return jdbcTemplate.query("select * from exam where id = ?", EXAM_MAPPER, id).stream()
        .findFirst();
  }

  @Override
  public List<Exam> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from exam where course_id = ? order by ordinal, id", EXAM_MAPPER, courseId);
  }

  @Override
  public ExamRegistration addRegistration(ExamRegistration registration) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into exam_registration (exam_id, student_id, registered_at, status)"
                + " values (?, ?, ?, ?)",
            registration.examId(),
            registration.studentId(),
            Timestamp.valueOf(registration.registeredAt()),
            registration.status());
    return new ExamRegistration(
        id,
        registration.examId(),
        registration.studentId(),
        registration.registeredAt(),
        registration.status());
  }

  @Override
  public List<ExamRegistration> findRegistrations(long examId) {
    return jdbcTemplate.query(
        "select * from exam_registration where exam_id = ? order by id",
        REGISTRATION_MAPPER,
        examId);
  }

  @Override
  public ExamRoom addRoom(ExamRoom room) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into exam_room (exam_id, room_id, capacity, required_assistants, reserved)"
                + " values (?, ?, ?, ?, ?)",
            room.examId(),
            room.roomId(),
            room.capacity(),
            room.requiredAssistants(),
            room.reserved());
    return new ExamRoom(
        id,
        room.examId(),
        room.roomId(),
        room.capacity(),
        room.requiredAssistants(),
        room.reserved());
  }

  @Override
  public List<ExamRoom> findRooms(long examId) {
    return jdbcTemplate.query(
        "select * from exam_room where exam_id = ? order by id", ROOM_MAPPER, examId);
  }

  @Override
  public void replaceSeats(long examId, List<ExamSeat> seats) {
    jdbcTemplate.update("delete from exam_seat where exam_id = ?", examId);
    for (ExamSeat seat : seats) {
      jdbcTemplate.update(
          "insert into exam_seat (exam_id, student_id, room_id, seat_no, test_group)"
              + " values (?, ?, ?, ?, ?)",
          examId,
          seat.studentId(),
          seat.roomId(),
          seat.seatNo(),
          seat.testGroup());
    }
  }

  @Override
  public List<ExamSeat> findSeats(long examId) {
    return jdbcTemplate.query(
        "select * from exam_seat where exam_id = ? order by room_id, id", SEAT_MAPPER, examId);
  }

  @Override
  public void markPublished(long examId, boolean published) {
    jdbcTemplate.update("update exam set published = ? where id = ?", published, examId);
  }

  private static Exam mapExam(ResultSet rs, int rowNum) throws SQLException {
    Timestamp startsAt = rs.getTimestamp("starts_at");
    Number prerequisite = (Number) rs.getObject("prerequisite_flag_id");
    return new Exam(
        rs.getLong("id"),
        rs.getLong("course_id"),
        rs.getString("title"),
        rs.getString("short_name"),
        ExamKind.valueOf(rs.getString("kind")),
        startsAt == null ? null : startsAt.toLocalDateTime(),
        rs.getInt("duration_minutes"),
        rs.getDouble("max_points"),
        rs.getInt("ordinal"),
        ExamVisibility.valueOf(rs.getString("visibility")),
        rs.getBoolean("locked"),
        prerequisite == null ? null : prerequisite.longValue(),
        rs.getBoolean("published"));
  }
}
