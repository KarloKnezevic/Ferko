package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.SurveyRepository;
import hr.fer.zemris.ferko.domain.model.Survey;
import hr.fer.zemris.ferko.domain.model.SurveyQuestion;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link SurveyRepository}. */
public class JdbcSurveyRepository implements SurveyRepository {

  private static final RowMapper<Survey> SURVEY_MAPPER =
      (rs, rowNum) ->
          new Survey(
              rs.getLong("id"),
              rs.getLong("course_id"),
              rs.getString("title"),
              rs.getBoolean("active"),
              rs.getTimestamp("created_at").toLocalDateTime());

  private static final RowMapper<SurveyQuestion> QUESTION_MAPPER =
      (rs, rowNum) ->
          new SurveyQuestion(
              rs.getLong("id"),
              rs.getLong("anketa_id"),
              rs.getString("text"),
              rs.getInt("ordinal"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcSurveyRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Survey createSurvey(Survey survey) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into anketa (course_id, title, active, created_at) values (?, ?, ?, ?)",
            survey.courseId(),
            survey.title(),
            survey.active(),
            survey.createdAt());
    return new Survey(id, survey.courseId(), survey.title(), survey.active(), survey.createdAt());
  }

  @Override
  public SurveyQuestion addQuestion(SurveyQuestion question) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into anketa_pitanje (anketa_id, text, ordinal) values (?, ?, ?)",
            question.surveyId(),
            question.text(),
            question.ordinal());
    return new SurveyQuestion(id, question.surveyId(), question.text(), question.ordinal());
  }

  @Override
  public List<Survey> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from anketa where course_id = ? order by created_at desc",
        SURVEY_MAPPER,
        courseId);
  }

  @Override
  public List<SurveyQuestion> findQuestions(long surveyId) {
    return jdbcTemplate.query(
        "select * from anketa_pitanje where anketa_id = ? order by ordinal, id",
        QUESTION_MAPPER,
        surveyId);
  }

  @Override
  public void addResponse(long questionId, int rating) {
    jdbcTemplate.update(
        "insert into anketa_odgovor (pitanje_id, ocjena) values (?, ?)", questionId, rating);
  }

  @Override
  public List<QuestionRating> ratingStats(long surveyId) {
    return jdbcTemplate.query(
        "select p.id as question_id, count(o.id) as cnt,"
            + " coalesce(avg(cast(o.ocjena as double precision)), 0) as avg_rating"
            + " from anketa_pitanje p left join anketa_odgovor o on o.pitanje_id = p.id"
            + " where p.anketa_id = ? group by p.id order by p.id",
        (rs, rowNum) ->
            new QuestionRating(
                rs.getLong("question_id"), rs.getLong("cnt"), rs.getDouble("avg_rating")),
        surveyId);
  }
}
