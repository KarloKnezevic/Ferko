package hr.fer.zemris.ferko.application.usecase.survey;

import java.util.List;

/** Read models for course evaluation surveys. */
public final class SurveyViews {

  private SurveyViews() {}

  public record QuestionView(long id, String text, int ordinal) {}

  public record SurveyView(
      long id, long courseId, String title, boolean active, List<QuestionView> questions) {}

  public record SurveyResultView(long questionId, String text, long responses, double average) {}

  /** A single answer submitted by a respondent. */
  public record AnswerInput(long questionId, int rating) {}
}
