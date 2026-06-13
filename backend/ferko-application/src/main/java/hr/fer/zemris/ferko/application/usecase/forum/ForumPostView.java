package hr.fer.zemris.ferko.application.usecase.forum;

import java.time.LocalDateTime;

/** Read model for a course discussion post. */
public record ForumPostView(
    long id,
    long courseId,
    Long parentId,
    String authorName,
    String body,
    LocalDateTime createdAt) {}
