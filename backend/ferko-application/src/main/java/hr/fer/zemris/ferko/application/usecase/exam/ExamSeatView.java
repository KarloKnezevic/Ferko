package hr.fer.zemris.ferko.application.usecase.exam;

/** Read projection of a single student's exam seat (room placement). */
public record ExamSeatView(
    long studentId,
    String studentJmbag,
    String studentFullName,
    long roomId,
    String roomCode,
    Integer seatNo,
    String testGroup) {}
