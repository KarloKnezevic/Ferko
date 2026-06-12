package hr.fer.zemris.ferko.domain.model;

/** A lecture or lab group on a course. */
public record StudentGroup(
    long id, long courseId, String groupCode, GroupType type, String category, int capacity) {}
