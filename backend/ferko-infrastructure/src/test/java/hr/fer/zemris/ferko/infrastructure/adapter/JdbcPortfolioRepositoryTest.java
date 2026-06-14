package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.PortfolioEntry;
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

class JdbcPortfolioRepositoryTest {

  private JdbcPortfolioRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:eportfolio_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcPortfolioRepository(jdbcTemplate);
  }

  @Test
  void savesScopesByUserAndRemoves() {
    PortfolioEntry mine =
        repository.save(
            new PortfolioEntry(
                0L, 100L, "Projekt", "Opis", "PROJEKT", "http://x", LocalDateTime.now()));
    repository.save(new PortfolioEntry(0L, 200L, "Tuđe", "", "", "", LocalDateTime.now()));

    assertTrue(mine.id() > 0);
    assertEquals(1, repository.findByUser(100L).size());
    assertEquals("Projekt", repository.findByUser(100L).get(0).title());

    // Removal is scoped to the owner: another user's id cannot delete it.
    repository.remove(200L, mine.id());
    assertEquals(1, repository.findByUser(100L).size());
    repository.remove(100L, mine.id());
    assertTrue(repository.findByUser(100L).isEmpty());
    assertEquals(1, repository.findByUser(200L).size());
  }
}
