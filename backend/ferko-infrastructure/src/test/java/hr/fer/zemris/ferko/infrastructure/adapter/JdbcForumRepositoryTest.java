package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.ForumPost;
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

class JdbcForumRepositoryTest {

  private JdbcForumRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:forum_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcForumRepository(jdbcTemplate);
  }

  @Test
  void savesQuestionsAndAnswers() {
    ForumPost question =
        repository.save(
            new ForumPost(
                0L, 100L, null, "student.ana", "Kako riješiti zadatak 3?", LocalDateTime.now()));
    assertTrue(question.id() > 0);
    assertNull(question.parentId());

    repository.save(
        new ForumPost(
            0L, 100L, question.id(), "lecturer.marko", "Koristite teorem.", LocalDateTime.now()));

    List<ForumPost> posts = repository.findByCourse(100L);
    assertEquals(2, posts.size());
    assertEquals(question.id(), posts.get(1).parentId());
    assertTrue(repository.findByCourse(999L).isEmpty());
  }
}
