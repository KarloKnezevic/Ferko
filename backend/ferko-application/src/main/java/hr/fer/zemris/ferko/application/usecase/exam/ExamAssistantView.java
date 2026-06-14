package hr.fer.zemris.ferko.application.usecase.exam;

/** A single invigilator ("dežurni") assigned to a room of an assessment. */
public record ExamAssistantView(
    long id,
    long examId,
    long roomId,
    String roomCode,
    long userId,
    String username,
    String fullName) {}
