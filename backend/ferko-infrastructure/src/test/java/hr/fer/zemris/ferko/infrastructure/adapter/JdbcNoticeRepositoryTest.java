package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.Notice;
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

class JdbcNoticeRepositoryTest {

  private JdbcNoticeRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:notice_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcNoticeRepository(jdbcTemplate);
  }

  @Test
  void savesAndListsNoticesPinnedFirst() {
    LocalDateTime now = LocalDateTime.now();
    repository.save(new Notice(0L, null, "Obična", "tekst", "admin", now.minusHours(1), false));
    Notice pinned =
        repository.save(new Notice(0L, null, "Prikvačena", "tekst", "admin", now, true));
    repository.save(new Notice(0L, 100L, "Kolegij", "tekst", "lecturer", now, false));

    assertTrue(pinned.id() > 0);

    List<Notice> recent = repository.findRecent(10);
    assertEquals(3, recent.size());
    assertEquals("Prikvačena", recent.get(0).title(), "pinned notice must come first");

    List<Notice> course = repository.findByCourse(100L);
    assertEquals(1, course.size());
    assertEquals(100L, course.get(0).courseId());

    // Faculty-wide notices keep a null course id.
    assertNull(recent.get(0).courseId());
  }

  @Test
  void recentLimitIsHonoured() {
    for (int i = 0; i < 5; i++) {
      repository.save(new Notice(0L, null, "N" + i, "tekst", "admin", LocalDateTime.now(), false));
    }
    assertEquals(2, repository.findRecent(2).size());
  }
}
