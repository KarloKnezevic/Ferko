package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.Grade;
import hr.fer.zemris.ferko.domain.model.GradeComponent;
import hr.fer.zemris.ferko.domain.model.StudentPoints;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class JdbcGradingRepositoryTest {

  private JdbcGradingRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:grading_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcGradingRepository(jdbcTemplate);
  }

  @Test
  void persistsComponentsPointsAndGrades() {
    GradeComponent component =
        repository.addComponent(new GradeComponent(0L, 100L, "Međuispit", "MI", 20.0, 1));
    assertTrue(component.id() > 0);
    assertEquals(1, repository.componentsByCourse(100L).size());

    repository.savePoints(
        new StudentPoints(
            0L, 100L, 5L, component.id(), null, 14.0, 20.0, true, "lecturer", LocalDateTime.now()));
    assertEquals(1, repository.pointsByCourse(100L).size());
    assertEquals(14.0, repository.pointsByCourse(100L).get(0).points());

    // Upsert: same (course, student, component) updates rather than duplicates.
    repository.savePoints(
        new StudentPoints(
            0L, 100L, 5L, component.id(), null, 18.0, 20.0, true, "lecturer", LocalDateTime.now()));
    assertEquals(1, repository.pointsByCourse(100L).size());
    assertEquals(18.0, repository.pointsByCourse(100L).get(0).points());

    repository.saveGrade(new Grade(0L, 100L, 5L, 4, 18.0, "lecturer", LocalDateTime.now()));
    assertEquals(4, repository.gradeFor(100L, 5L).orElseThrow().finalGrade());
    // Upsert grade.
    repository.saveGrade(new Grade(0L, 100L, 5L, 5, 18.0, "lecturer", LocalDateTime.now()));
    assertEquals(1, repository.gradesByCourse(100L).size());
    assertEquals(5, repository.gradeFor(100L, 5L).orElseThrow().finalGrade());
  }
}
