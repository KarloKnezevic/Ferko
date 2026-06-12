package hr.fer.zemris.ferko.application.usecase.academic;

/** Read projection of a staff assignment on a course. */
public record CourseStaffView(long userId, String fullName, String role) {}
