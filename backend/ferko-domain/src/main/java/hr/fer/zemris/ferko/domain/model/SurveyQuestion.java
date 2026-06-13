package hr.fer.zemris.ferko.domain.model;

/** A single Likert question (rated 1..5) within a {@link Survey}. */
public record SurveyQuestion(long id, long surveyId, String text, int ordinal) {}
