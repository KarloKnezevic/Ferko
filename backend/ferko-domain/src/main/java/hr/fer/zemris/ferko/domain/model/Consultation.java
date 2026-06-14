package hr.fer.zemris.ferko.domain.model;

import java.time.LocalTime;

/** A weekly office-hours slot ("konzultacije") published by teaching staff for a course. */
public record Consultation(
    long id,
    long courseId,
    String staffName,
    String dayOfWeek,
    LocalTime startsAt,
    LocalTime endsAt,
    String location) {}
