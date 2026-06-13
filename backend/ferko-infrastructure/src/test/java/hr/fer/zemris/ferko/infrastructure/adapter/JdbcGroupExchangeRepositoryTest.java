package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.ExchangeStatus;
import hr.fer.zemris.ferko.domain.model.GroupExchangeRequest;
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

class JdbcGroupExchangeRepositoryTest {

  private JdbcGroupExchangeRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:burza_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcGroupExchangeRepository(jdbcTemplate);
  }

  @Test
  void savesPendingThenRecordsDecision() {
    GroupExchangeRequest saved =
        repository.save(
            new GroupExchangeRequest(
                0L,
                100L,
                7L,
                1L,
                2L,
                ExchangeStatus.PENDING,
                "Posao",
                null,
                LocalDateTime.now(),
                null));
    assertTrue(saved.id() > 0);
    assertEquals(ExchangeStatus.PENDING, saved.status());
    assertEquals(1, repository.findByCourse(100L).size());

    repository.updateDecision(
        saved.id(), ExchangeStatus.APPROVED, "stuslu.sara", LocalDateTime.now());
    GroupExchangeRequest after = repository.findById(saved.id()).orElseThrow();
    assertEquals(ExchangeStatus.APPROVED, after.status());
    assertEquals("stuslu.sara", after.decidedBy());
  }
}
