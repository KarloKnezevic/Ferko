package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/** An assessment ("provjera znanja") on a course. */
public record Exam(
    long id,
    long courseId,
    String title,
    String shortName,
    ExamKind kind,
    LocalDateTime startsAt,
    int durationMinutes,
    double maxPoints,
    int ordinal,
    ExamVisibility visibility,
    boolean locked,
    Long prerequisiteFlagId,
    boolean published) {}
