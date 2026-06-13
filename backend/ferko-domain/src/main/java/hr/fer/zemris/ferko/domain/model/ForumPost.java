package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/**
 * A post in a course discussion ("Pitanja i problemi"). A {@code null} {@code parentId} is a
 * top-level question; otherwise the post answers the referenced post.
 */
public record ForumPost(
    long id,
    long courseId,
    Long parentId,
    String authorName,
    String body,
    LocalDateTime createdAt) {}
