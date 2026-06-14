package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.ExamRoomAssistant;
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

class JdbcExamAssistantRepositoryTest {

  private JdbcExamAssistantRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:assistants_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcExamAssistantRepository(jdbcTemplate);
  }

  @Test
  void assignsListsAndRemoves() {
    ExamRoomAssistant first = repository.assign(new ExamRoomAssistant(0L, 5L, 10L, 100L));
    repository.assign(new ExamRoomAssistant(0L, 5L, 11L, 101L));
    repository.assign(new ExamRoomAssistant(0L, 9L, 10L, 100L));

    assertTrue(first.id() > 0);
    List<ExamRoomAssistant> forExam = repository.findByExam(5L);
    assertEquals(2, forExam.size());
    assertEquals(10L, forExam.get(0).roomId());

    repository.remove(5L, first.id());
    assertEquals(1, repository.findByExam(5L).size());
    // removal is scoped to the exam: the same id under a different exam is untouched
    assertEquals(1, repository.findByExam(9L).size());
  }
}
