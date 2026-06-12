package hr.fer.zemris.ferko.application.usecase.exam;

import java.time.LocalDateTime;

/** Read projection of an assessment. */
public record ExamView(
    long id,
    long courseId,
    String title,
    String shortName,
    String kind,
    LocalDateTime startsAt,
    int durationMinutes,
    double maxPoints,
    boolean published,
    int registeredStudents,
    int reservedRooms,
    int totalRoomCapacity,
    int seatedStudents) {}
