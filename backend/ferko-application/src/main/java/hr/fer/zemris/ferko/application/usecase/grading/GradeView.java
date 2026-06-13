package hr.fer.zemris.ferko.application.usecase.grading;

/** Read projection of a student's final grade on a course. */
public record GradeView(
    long studentId, String jmbag, String fullName, int finalGrade, double pointsTotal) {}
