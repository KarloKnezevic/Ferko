package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.Demonstrator;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class JdbcDemonstratorRepositoryTest {

  private JdbcDemonstratorRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:demo_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcDemonstratorRepository(jdbcTemplate);
  }

  @Test
  void savesFindsAndDeletes() {
    Demonstrator saved = repository.save(new Demonstrator(0L, 10L, 200L));
    assertTrue(saved.id() > 0);

    assertEquals(1, repository.findByCourse(10L).size());
    assertEquals(1, repository.findByStudent(200L).size());
    assertTrue(repository.exists(10L, 200L));
    assertFalse(repository.exists(10L, 999L));

    assertTrue(repository.delete(10L, 200L));
    assertFalse(repository.delete(10L, 200L));
    assertTrue(repository.findByCourse(10L).isEmpty());
  }
}
