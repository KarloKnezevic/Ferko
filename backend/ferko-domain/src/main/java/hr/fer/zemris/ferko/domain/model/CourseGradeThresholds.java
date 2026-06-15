package hr.fer.zemris.ferko.domain.model;

/** Per-course grade thresholds overriding the global defaults (points percentage cut-offs). */
public record CourseGradeThresholds(
    long courseId, int excellent, int veryGood, int good, int sufficient) {}
