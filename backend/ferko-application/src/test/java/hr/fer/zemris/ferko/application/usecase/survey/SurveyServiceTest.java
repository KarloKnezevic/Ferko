package hr.fer.zemris.ferko.application.usecase.survey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.SurveyRepository;
import hr.fer.zemris.ferko.domain.model.Survey;
import hr.fer.zemris.ferko.domain.model.SurveyQuestion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class SurveyServiceTest {

  private static final class FakeSurveyRepository implements SurveyRepository {
    private final List<Survey> surveys = new ArrayList<>();
    private final List<SurveyQuestion> questions = new ArrayList<>();
    private final Map<Long, List<Integer>> ratings = new TreeMap<>();
    private long surveySeq = 0;
    private long questionSeq = 0;

    @Override
    public Survey createSurvey(Survey survey) {
      Survey saved =
          new Survey(
              ++surveySeq, survey.courseId(), survey.title(), survey.active(), survey.createdAt());
      surveys.add(saved);
      return saved;
    }

    @Override
    public SurveyQuestion addQuestion(SurveyQuestion question) {
      SurveyQuestion saved =
          new SurveyQuestion(
              ++questionSeq, question.surveyId(), question.text(), question.ordinal());
      questions.add(saved);
      return saved;
    }

    @Override
    public List<Survey> findByCourse(long courseId) {
      return surveys.stream().filter(s -> s.courseId() == courseId).toList();
    }

    @Override
    public List<SurveyQuestion> findQuestions(long surveyId) {
      return questions.stream().filter(q -> q.surveyId() == surveyId).toList();
    }

    @Override
    public void addResponse(long questionId, int rating) {
      ratings.computeIfAbsent(questionId, k -> new ArrayList<>()).add(rating);
    }

    @Override
    public List<QuestionRating> ratingStats(long surveyId) {
      List<QuestionRating> out = new ArrayList<>();
      for (SurveyQuestion q : findQuestions(surveyId)) {
        List<Integer> rs = ratings.getOrDefault(q.id(), List.of());
        double avg = rs.stream().mapToInt(Integer::intValue).average().orElse(0);
        out.add(new QuestionRating(q.id(), rs.size(), avg));
      }
      return out;
    }
  }

  @Test
  void createsCollectsAndAggregates() {
    SurveyService service = new SurveyService(new FakeSurveyRepository());
    long surveyId = service.createSurvey(7L, "Evaluacija", List.of("Jasnoća", "Tempo"));
    assertTrue(surveyId > 0);

    SurveyViews.SurveyView view = service.listForCourse(7L).get(0);
    assertEquals(2, view.questions().size());
    long q1 = view.questions().get(0).id();
    long q2 = view.questions().get(1).id();

    service.submit(
        surveyId, List.of(new SurveyViews.AnswerInput(q1, 5), new SurveyViews.AnswerInput(q2, 3)));
    service.submit(surveyId, List.of(new SurveyViews.AnswerInput(q1, 3)));

    Map<Long, SurveyViews.SurveyResultView> byQuestion = new TreeMap<>();
    service.results(surveyId).forEach(r -> byQuestion.put(r.questionId(), r));
    assertEquals(2, byQuestion.get(q1).responses());
    assertEquals(4.0, byQuestion.get(q1).average(), 1e-9);
    assertEquals(1, byQuestion.get(q2).responses());
  }

  @Test
  void rejectsInvalidRatingAndForeignQuestion() {
    SurveyService service = new SurveyService(new FakeSurveyRepository());
    long surveyId = service.createSurvey(7L, "Evaluacija", List.of("Jasnoća"));
    long q1 = service.listForCourse(7L).get(0).questions().get(0).id();

    assertThrows(
        IllegalArgumentException.class,
        () -> service.submit(surveyId, List.of(new SurveyViews.AnswerInput(q1, 9))));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.submit(surveyId, List.of(new SurveyViews.AnswerInput(99999L, 3))));
    assertThrows(
        IllegalArgumentException.class, () -> service.createSurvey(7L, "Prazna", List.of()));
  }
}
