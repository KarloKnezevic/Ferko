package hr.fer.zemris.ferko.application.usecase.student;

import java.time.LocalDateTime;

/**
 * One assessment as seen by the enrolled student: the exam metadata plus, once the schedule is
 * published, the room and seat assigned to that student. Room and seat stay {@code null} until the
 * assessment is published.
 */
public record MyExamView(
    long examId,
    long courseId,
    String courseCode,
    String courseName,
    String title,
    String shortName,
    String kind,
    LocalDateTime startsAt,
    int durationMinutes,
    double maxPoints,
    boolean registered,
    boolean published,
    String roomCode,
    Integer seatNo,
    String testGroup) {}
