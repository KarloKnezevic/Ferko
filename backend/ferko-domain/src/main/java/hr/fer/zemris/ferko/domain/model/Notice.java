package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/**
 * An announcement ("obavijest") shown on the portal. A {@code null} {@code courseId} denotes a
 * faculty-wide notice; otherwise the notice belongs to a specific course. Pinned notices are shown
 * first.
 */
public record Notice(
    long id,
    Long courseId,
    String title,
    String body,
    String authorName,
    LocalDateTime createdAt,
    boolean pinned) {}
