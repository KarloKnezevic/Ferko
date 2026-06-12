package hr.fer.zemris.ferko.domain.model;

/** Assignment of a staff member to a course in a specific role. */
public record CourseStaff(long id, long courseId, long userId, Role role) {}
