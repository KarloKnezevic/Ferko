package hr.fer.zemris.ferko.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/** A recurring lecture or lab slot in the weekly timetable. */
public record ClassSchedule(
    long id,
    long courseId,
    Long groupId,
    GroupType type,
    Long roomId,
    DayOfWeek dayOfWeek,
    LocalTime startsAt,
    LocalTime endsAt,
    String instructor) {}
