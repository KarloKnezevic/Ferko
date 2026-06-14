package hr.fer.zemris.ferko.application.usecase.literature;

/** Read model for a course reading-list entry. */
public record CourseLiteratureView(
    long id, long courseId, String title, String author, boolean mandatory, int ordinal) {}
