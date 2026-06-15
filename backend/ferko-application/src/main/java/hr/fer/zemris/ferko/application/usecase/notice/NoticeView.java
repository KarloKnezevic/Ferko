package hr.fer.zemris.ferko.application.usecase.notice;

import java.time.LocalDateTime;

/** Read model for an announcement shown on the portal. */
public record NoticeView(
    long id,
    Long courseId,
    String title,
    String body,
    String authorName,
    LocalDateTime createdAt,
    boolean pinned,
    boolean canDelete) {}
