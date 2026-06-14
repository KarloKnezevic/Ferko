package hr.fer.zemris.ferko.domain.model;

/** A student assigned to assist a course's laboratory exercises ("demonstrator"). */
public record Demonstrator(long id, long courseId, long studentId) {}
