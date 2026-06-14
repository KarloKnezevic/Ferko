package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.ExamAssistantRepository;
import hr.fer.zemris.ferko.domain.model.ExamRoomAssistant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link ExamAssistantRepository} (invigilator assignments per exam room). */
public class JdbcExamAssistantRepository implements ExamAssistantRepository {

  private static final RowMapper<ExamRoomAssistant> MAPPER =
      (rs, rowNum) ->
          new ExamRoomAssistant(
              rs.getLong("id"),
              rs.getLong("exam_id"),
              rs.getLong("room_id"),
              rs.getLong("app_user_id"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcExamAssistantRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public ExamRoomAssistant assign(ExamRoomAssistant assignment) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into exam_room_assistant (exam_id, room_id, app_user_id) values (?, ?, ?)",
            assignment.examId(),
            assignment.roomId(),
            assignment.userId());
    return new ExamRoomAssistant(id, assignment.examId(), assignment.roomId(), assignment.userId());
  }

  @Override
  public List<ExamRoomAssistant> findByExam(long examId) {
    return jdbcTemplate.query(
        "select * from exam_room_assistant where exam_id = ? order by room_id, id", MAPPER, examId);
  }

  @Override
  public List<ExamRoomAssistant> findByUser(long userId) {
    return jdbcTemplate.query(
        "select * from exam_room_assistant where app_user_id = ? order by exam_id, id",
        MAPPER,
        userId);
  }

  @Override
  public void remove(long examId, long assignmentId) {
    jdbcTemplate.update(
        "delete from exam_room_assistant where id = ? and exam_id = ?", assignmentId, examId);
  }
}
