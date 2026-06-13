package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.SurveyRepository.QuestionRating;
import hr.fer.zemris.ferko.domain.model.Survey;
import hr.fer.zemris.ferko.domain.model.SurveyQuestion;
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

class JdbcSurveyRepositoryTest {

  private JdbcSurveyRepository repository;

  @BeforeEach
  void setUp() throws SQLException {
    String url =
        "jdbc:h2:mem:survey_"
            + UUID.randomUUID().toString().replace("-", "")
            + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
    repository = new JdbcSurveyRepository(jdbcTemplate);
  }

  @Test
  void createsSurveyWithQuestionsAndAggregatesResponses() {
    Survey survey =
        repository.createSurvey(new Survey(0L, 100L, "Evaluacija", true, LocalDateTime.now()));
    assertTrue(survey.id() > 0);

    SurveyQuestion q1 = repository.addQuestion(new SurveyQuestion(0L, survey.id(), "Jasnoća", 0));
    SurveyQuestion q2 = repository.addQuestion(new SurveyQuestion(0L, survey.id(), "Tempo", 1));

    assertEquals(2, repository.findQuestions(survey.id()).size());
    assertEquals(1, repository.findByCourse(100L).size());

    repository.addResponse(q1.id(), 5);
    repository.addResponse(q1.id(), 3);
    repository.addResponse(q2.id(), 4);

    List<QuestionRating> stats = repository.ratingStats(survey.id());
    assertEquals(2, stats.size());
    QuestionRating first =
        stats.stream().filter(s -> s.questionId() == q1.id()).findFirst().orElseThrow();
    assertEquals(2, first.count());
    assertEquals(4.0, first.average(), 1e-9);

    QuestionRating second =
        stats.stream().filter(s -> s.questionId() == q2.id()).findFirst().orElseThrow();
    assertEquals(1, second.count());
    assertEquals(4.0, second.average(), 1e-9);
  }
}
