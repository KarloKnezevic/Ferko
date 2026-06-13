package hr.fer.zemris.ferko.application.usecase.grading;

/** Read projection of a scored grade component (e.g. midterm, lab). */
public record GradeComponentView(
    long id, String name, String shortName, double maxPoints, int ordinal) {}
