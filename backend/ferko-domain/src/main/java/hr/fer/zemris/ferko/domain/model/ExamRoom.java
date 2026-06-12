package hr.fer.zemris.ferko.domain.model;

/** A room reserved for an assessment, with its usable capacity. */
public record ExamRoom(
    long id, long examId, long roomId, int capacity, int requiredAssistants, boolean reserved) {}
