package hr.fer.zemris.ferko.domain.model;

/** An assistant ("dežurni") assigned to invigilate one room of an assessment. */
public record ExamRoomAssistant(long id, long examId, long roomId, long userId) {}
