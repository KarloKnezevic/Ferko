package hr.fer.zemris.ferko.application.usecase.component;

/** Read model for a course content component. */
public record CourseComponentView(
    long id, long courseId, String title, String content, int ordinal, boolean visible) {}
