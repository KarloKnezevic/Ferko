package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.ConsultationRepository;
import hr.fer.zemris.ferko.domain.model.Consultation;
import java.sql.Time;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link ConsultationRepository}. */
public class JdbcConsultationRepository implements ConsultationRepository {

  private static final RowMapper<Consultation> MAPPER =
      (rs, rowNum) ->
          new Consultation(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getString("staff_name"),
              rs.getString("day_of_week"),
              rs.getTime("starts_at").toLocalTime(),
              rs.getTime("ends_at").toLocalTime(),
              rs.getString("location"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcConsultationRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Consultation save(Consultation consultation) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into konzultacije (course_id, staff_name, day_of_week, starts_at, ends_at,"
                + " location) values (?, ?, ?, ?, ?, ?)",
            consultation.courseId(),
            consultation.staffName(),
            consultation.dayOfWeek(),
            Time.valueOf(consultation.startsAt()),
            Time.valueOf(consultation.endsAt()),
            consultation.location());
    return new Consultation(
        id,
        consultation.courseId(),
        consultation.staffName(),
        consultation.dayOfWeek(),
        consultation.startsAt(),
        consultation.endsAt(),
        consultation.location());
  }

  @Override
  public List<Consultation> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from konzultacije where course_id = ? order by starts_at, id", MAPPER, courseId);
  }

  @Override
  public void remove(long courseId, long consultationId) {
    jdbcTemplate.update(
        "delete from konzultacije where id = ? and course_id = ?", consultationId, courseId);
  }
}
