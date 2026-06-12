package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link StudentRepository}. */
public class JdbcStudentRepository implements StudentRepository {

  private static final RowMapper<Student> MAPPER =
      (rs, rowNum) ->
          new Student(
              rs.getLong("id"),
              rs.getLong("user_id"),
              rs.getString("jmbag"),
              rs.getString("study_program"),
              rs.getInt("year_of_study"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcStudentRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Student save(Student student) {
    long id = student.id();
    if (id <= 0) {
      id =
          JdbcIds.insert(
              jdbcTemplate,
              "insert into student (user_id, jmbag, study_program, year_of_study)"
                  + " values (?, ?, ?, ?)",
              student.userId(),
              student.jmbag(),
              student.studyProgram(),
              student.yearOfStudy());
    } else {
      jdbcTemplate.update(
          "update student set user_id = ?, jmbag = ?, study_program = ?, year_of_study = ?"
              + " where id = ?",
          student.userId(),
          student.jmbag(),
          student.studyProgram(),
          student.yearOfStudy(),
          id);
    }
    return findById(id).orElseThrow();
  }

  @Override
  public Optional<Student> findById(long id) {
    return jdbcTemplate.query("select * from student where id = ?", MAPPER, id).stream()
        .findFirst();
  }

  @Override
  public Optional<Student> findByJmbag(String jmbag) {
    return jdbcTemplate.query("select * from student where jmbag = ?", MAPPER, jmbag).stream()
        .findFirst();
  }

  @Override
  public Optional<Student> findByUserId(long userId) {
    return jdbcTemplate.query("select * from student where user_id = ?", MAPPER, userId).stream()
        .findFirst();
  }

  @Override
  public List<Student> findAll() {
    return jdbcTemplate.query("select * from student order by jmbag", MAPPER);
  }
}
