package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.AcademicAuditEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class JdbcAuditEventRepositoryTest {

  private JdbcAuditEventRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:audit_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcAuditEventRepository(jdbcTemplate);
  }

  @Test
  void savesAndReadsRecentNewestFirstWithLimit() {
    repository.save(
        new AcademicAuditEvent(
            0L, LocalDateTime.now().minusMinutes(2), "admin", "A1", "course", "1", "d1"));
    AcademicAuditEvent newer =
        repository.save(
            new AcademicAuditEvent(0L, LocalDateTime.now(), "admin", "A2", "course", "2", "d2"));

    assertTrue(newer.id() > 0);
    List<AcademicAuditEvent> recent = repository.recent(10);
    assertEquals(2, recent.size());
    assertEquals("A2", recent.get(0).action(), "newest first");

    assertEquals(1, repository.recent(1).size(), "limit applied");
  }
}
