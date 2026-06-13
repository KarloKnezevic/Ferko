package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.RepositoryFile;
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

class JdbcRepositoryFileRepositoryTest {

  private JdbcRepositoryFileRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:repo_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcRepositoryFileRepository(jdbcTemplate);
  }

  @Test
  void savesAndReadsFileMetadata() {
    RepositoryFile saved =
        repository.save(
            new RepositoryFile(
                0L,
                100L,
                "skripta.pdf",
                "application/pdf",
                1234,
                "key-1",
                "lecturer.marko",
                LocalDateTime.now()));
    assertTrue(saved.id() > 0);

    assertEquals(1, repository.findByCourse(100L).size());
    RepositoryFile found = repository.findById(saved.id()).orElseThrow();
    assertEquals("skripta.pdf", found.filename());
    assertEquals(1234, found.sizeBytes());
    assertEquals("key-1", found.storageKey());
    assertTrue(repository.findByCourse(999L).isEmpty());
  }
}
