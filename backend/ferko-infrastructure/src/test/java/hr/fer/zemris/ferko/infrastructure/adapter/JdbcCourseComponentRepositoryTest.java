package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.CourseComponent;
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

class JdbcCourseComponentRepositoryTest {

  private JdbcCourseComponentRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:comp_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcCourseComponentRepository(jdbcTemplate);
  }

  @Test
  void savesAndOrdersComponents() {
    repository.save(new CourseComponent(0L, 100L, "Literatura", "Knjiga X", 1, true));
    CourseComponent first =
        repository.save(new CourseComponent(0L, 100L, "O kolegiju", "Opis", 0, true));
    repository.save(new CourseComponent(0L, 100L, "Skriveno", "tajna", 2, false));

    assertTrue(first.id() > 0);

    List<CourseComponent> components = repository.findByCourse(100L);
    assertEquals(3, components.size());
    assertEquals("O kolegiju", components.get(0).title(), "ordered by ordinal");
    assertEquals("Literatura", components.get(1).title());
    assertTrue(repository.findByCourse(999L).isEmpty());
  }
}
