package hr.fer.zemris.ferko.application.usecase.survey;

import hr.fer.zemris.ferko.application.port.SurveyRepository;
import hr.fer.zemris.ferko.domain.model.Survey;
import hr.fer.zemris.ferko.domain.model.SurveyQuestion;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Creates surveys, collects responses and aggregates per-question results. */
public class SurveyService {

  private final SurveyRepository surveyRepository;

  public SurveyService(SurveyRepository surveyRepository) {
    this.surveyRepository = surveyRepository;
  }

  public long createSurvey(long courseId, String title, List<String> questions) {
    if (questions == null || questions.isEmpty()) {
      throw new IllegalArgumentException("Anketa mora imati barem jedno pitanje.");
    }
    Survey survey =
        surveyRepository.createSurvey(new Survey(0L, courseId, title, true, LocalDateTime.now()));
    int ordinal = 0;
    for (String text : questions) {
      surveyRepository.addQuestion(new SurveyQuestion(0L, survey.id(), text, ordinal++));
    }
    return survey.id();
  }

  public List<SurveyViews.SurveyView> listForCourse(long courseId) {
    return surveyRepository.findByCourse(courseId).stream()
        .map(
            survey ->
                new SurveyViews.SurveyView(
                    survey.id(),
                    survey.courseId(),
                    survey.title(),
                    survey.active(),
                    surveyRepository.findQuestions(survey.id()).stream()
                        .map(q -> new SurveyViews.QuestionView(q.id(), q.text(), q.ordinal()))
                        .toList()))
        .toList();
  }

  public void submit(long surveyId, List<SurveyViews.AnswerInput> answers) {
    Set<Long> validQuestions =
        surveyRepository.findQuestions(surveyId).stream()
            .map(SurveyQuestion::id)
            .collect(Collectors.toCollection(HashSet::new));
    for (SurveyViews.AnswerInput answer : answers) {
      if (!validQuestions.contains(answer.questionId())) {
        throw new IllegalArgumentException("Pitanje ne pripada ovoj anketi.");
      }
      if (answer.rating() < 1 || answer.rating() > 5) {
        throw new IllegalArgumentException("Ocjena mora biti između 1 i 5.");
      }
    }
    for (SurveyViews.AnswerInput answer : answers) {
      surveyRepository.addResponse(answer.questionId(), answer.rating());
    }
  }

  public List<SurveyViews.SurveyResultView> results(long surveyId) {
    Map<Long, String> questionText =
        surveyRepository.findQuestions(surveyId).stream()
            .collect(Collectors.toMap(SurveyQuestion::id, SurveyQuestion::text));
    return surveyRepository.ratingStats(surveyId).stream()
        .map(
            stat ->
                new SurveyViews.SurveyResultView(
                    stat.questionId(),
                    questionText.getOrDefault(stat.questionId(), ""),
                    stat.count(),
                    Math.round(stat.average() * 100.0) / 100.0))
        .toList();
  }
}
