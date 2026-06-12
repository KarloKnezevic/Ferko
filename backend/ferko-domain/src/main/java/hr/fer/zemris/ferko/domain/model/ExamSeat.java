package hr.fer.zemris.ferko.domain.model;

/** A seating assignment produced by the scheduler: a student placed in a room. */
public record ExamSeat(
    long id, long examId, long studentId, long roomId, Integer seatNo, String testGroup) {}
