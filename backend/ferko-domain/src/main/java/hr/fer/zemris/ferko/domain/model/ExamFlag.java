package hr.fer.zemris.ferko.domain.model;

/** A flag / prerequisite ("zastavica") evaluated per student on a course. */
public record ExamFlag(long id, long courseId, String name, String shortName, String description) {}
