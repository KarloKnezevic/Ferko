package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.SemesterRepository;
import hr.fer.zemris.ferko.domain.model.Semester;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link SemesterRepository}. */
public class JdbcSemesterRepository implements SemesterRepository {

  private static final RowMapper<Semester> MAPPER =
      (rs, rowNum) ->
          new Semester(
              rs.getString("code"),
              rs.getString("academic_year"),
              rs.getString("term"),
              rs.getDate("starts_on").toLocalDate(),
              rs.getDate("ends_on").toLocalDate(),
              rs.getBoolean("active"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcSemesterRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Semester save(Semester semester) {
    int updated =
        jdbcTemplate.update(
            "update semester set academic_year = ?, term = ?, starts_on = ?, ends_on = ?,"
                + " active = ? where code = ?",
            semester.academicYear(),
            semester.term(),
            semester.startsOn(),
            semester.endsOn(),
            semester.active(),
            semester.code());
    if (updated == 0) {
      jdbcTemplate.update(
          "insert into semester (code, academic_year, term, starts_on, ends_on, active)"
              + " values (?, ?, ?, ?, ?, ?)",
          semester.code(),
          semester.academicYear(),
          semester.term(),
          semester.startsOn(),
          semester.endsOn(),
          semester.active());
    }
    return semester;
  }

  @Override
  public Optional<Semester> findByCode(String code) {
    return jdbcTemplate.query("select * from semester where code = ?", MAPPER, code).stream()
        .findFirst();
  }

  @Override
  public Optional<Semester> findActive() {
    return jdbcTemplate.query("select * from semester where active = true", MAPPER).stream()
        .findFirst();
  }

  @Override
  public List<Semester> findAll() {
    return jdbcTemplate.query("select * from semester order by code", MAPPER);
  }
}
