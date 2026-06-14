package hr.fer.zemris.ferko.domain.model;

/**
 * A reading-list entry on a course page ("Literatura"). {@code mandatory} distinguishes required
 * ("obavezna") from recommended ("preporučena") literature; ordered by {@code ordinal}.
 */
public record CourseLiterature(
    long id, long courseId, String title, String author, boolean mandatory, int ordinal) {}
