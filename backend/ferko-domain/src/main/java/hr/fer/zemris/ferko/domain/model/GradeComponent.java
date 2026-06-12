package hr.fer.zemris.ferko.domain.model;

/** A scored component of a course's grade (e.g. midterm, lab, final). */
public record GradeComponent(
    long id, long courseId, String name, String shortName, double maxPoints, int ordinal) {}
