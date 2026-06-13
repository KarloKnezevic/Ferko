package hr.fer.zemris.ferko.application.usecase.grading;

import java.util.Map;

/**
 * One row of the "preglednik bodova": a student with their points per component short name, the
 * total, and the final grade (0 if not yet assigned).
 */
public record PointsOverviewRow(
    long studentId,
    String jmbag,
    String fullName,
    Map<String, Double> pointsByComponent,
    double total,
    int finalGrade) {}
