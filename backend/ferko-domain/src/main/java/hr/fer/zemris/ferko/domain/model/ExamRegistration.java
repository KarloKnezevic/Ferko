package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/** A student registered for an assessment. */
public record ExamRegistration(
    long id, long examId, long studentId, LocalDateTime registeredAt, String status) {}
