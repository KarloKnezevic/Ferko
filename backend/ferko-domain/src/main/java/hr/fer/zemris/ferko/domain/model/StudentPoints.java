package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/** Points awarded to a student for a grade component or assessment. */
public record StudentPoints(
    long id,
    long courseId,
    long studentId,
    Long componentId,
    Long examId,
    double points,
    double maxPoints,
    boolean published,
    String enteredBy,
    LocalDateTime enteredAt) {}
