package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Survey;
import hr.fer.zemris.ferko.domain.model.SurveyQuestion;
import java.util.List;

/** Persistence port for course evaluation surveys ("ankete"). */
public interface SurveyRepository {

  Survey createSurvey(Survey survey);

  SurveyQuestion addQuestion(SurveyQuestion question);

  List<Survey> findByCourse(long courseId);

  List<SurveyQuestion> findQuestions(long surveyId);

  void addResponse(long questionId, int rating);

  /** Aggregated rating per question for a survey. */
  List<QuestionRating> ratingStats(long surveyId);

  /** Aggregate of ratings for a single question. */
  record QuestionRating(long questionId, long count, double average) {}
}
