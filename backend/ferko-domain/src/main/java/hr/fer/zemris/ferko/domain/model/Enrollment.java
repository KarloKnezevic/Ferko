package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/** A student's enrollment in a course. */
public record Enrollment(
    long id, long studentId, long courseId, LocalDateTime enrolledAt, EnrollmentStatus status) {}
