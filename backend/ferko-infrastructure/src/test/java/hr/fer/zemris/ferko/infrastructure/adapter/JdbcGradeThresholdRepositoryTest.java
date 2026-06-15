package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.CourseGradeThresholds;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class JdbcGradeThresholdRepositoryTest {

  private JdbcGradeThresholdRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:thr_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcGradeThresholdRepository(jdbcTemplate);
  }

  @Test
  void upsertsThresholdsPerCourse() {
    assertTrue(repository.findByCourse(10L).isEmpty());

    repository.save(new CourseGradeThresholds(10L, 88, 75, 62, 50));
    CourseGradeThresholds stored = repository.findByCourse(10L).orElseThrow();
    assertEquals(88, stored.excellent());
    assertEquals(50, stored.sufficient());

    // Saving again updates in place (single row per course).
    repository.save(new CourseGradeThresholds(10L, 90, 78, 65, 51));
    CourseGradeThresholds updated = repository.findByCourse(10L).orElseThrow();
    assertEquals(90, updated.excellent());
    assertEquals(51, updated.sufficient());
  }
}
