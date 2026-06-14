package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.CourseLiterature;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class JdbcCourseLiteratureRepositoryTest {

  private JdbcCourseLiteratureRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:lit_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcCourseLiteratureRepository(jdbcTemplate);
  }

  @Test
  void savesAndOrdersEntries() {
    repository.save(new CourseLiterature(0L, 100L, "Preporučena", "Autor B", false, 1));
    CourseLiterature first =
        repository.save(new CourseLiterature(0L, 100L, "Obavezna", "Autor A", true, 0));

    assertTrue(first.id() > 0);

    List<CourseLiterature> entries = repository.findByCourse(100L);
    assertEquals(2, entries.size());
    assertEquals("Obavezna", entries.get(0).title(), "ordered by ordinal");
    assertTrue(entries.get(0).mandatory());
    assertFalse(entries.get(1).mandatory());
    assertTrue(repository.findByCourse(999L).isEmpty());
  }
}
