package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/** A student's final grade on a course. */
public record Grade(
    long id,
    long courseId,
    long studentId,
    int finalGrade,
    double pointsTotal,
    String decidedBy,
    LocalDateTime decidedAt) {}
