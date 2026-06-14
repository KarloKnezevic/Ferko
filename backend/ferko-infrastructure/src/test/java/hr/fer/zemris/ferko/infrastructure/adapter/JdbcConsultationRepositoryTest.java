package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.Consultation;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class JdbcConsultationRepositoryTest {

  private JdbcConsultationRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:konz_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcConsultationRepository(jdbcTemplate);
  }

  @Test
  void savesOrdersAndRemoves() {
    repository.save(
        new Consultation(0L, 100L, "B", "Utorak", LocalTime.of(14, 0), LocalTime.of(15, 0), "B-2"));
    Consultation early =
        repository.save(
            new Consultation(
                0L, 100L, "A", "Ponedjeljak", LocalTime.of(9, 0), LocalTime.of(10, 0), "A-1"));

    assertTrue(early.id() > 0);
    List<Consultation> list = repository.findByCourse(100L);
    assertEquals(2, list.size());
    assertEquals(LocalTime.of(9, 0), list.get(0).startsAt(), "ordered by start time");
    assertEquals("A-1", list.get(0).location());

    repository.remove(100L, early.id());
    assertEquals(1, repository.findByCourse(100L).size());
    assertTrue(repository.findByCourse(999L).isEmpty());
  }
}
