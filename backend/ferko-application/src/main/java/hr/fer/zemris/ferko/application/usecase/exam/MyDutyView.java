package hr.fer.zemris.ferko.application.usecase.exam;

import java.time.LocalDateTime;

/** One invigilation duty ("dežurstvo") of the signed-in staff member. */
public record MyDutyView(
    long examId,
    String examTitle,
    String examShortName,
    long courseId,
    String courseCode,
    String courseName,
    LocalDateTime startsAt,
    String roomCode,
    boolean published) {}
