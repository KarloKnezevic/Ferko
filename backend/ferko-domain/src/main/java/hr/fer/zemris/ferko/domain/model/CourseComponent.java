package hr.fer.zemris.ferko.domain.model;

/**
 * A titled content block on a course page ("KOMPONENTA"), e.g. "O kolegiju", "Literatura" or
 * "Pravila ocjenjivanja". Ordered by {@code ordinal}; hidden blocks have {@code visible = false}.
 */
public record CourseComponent(
    long id, long courseId, String title, String content, int ordinal, boolean visible) {}
