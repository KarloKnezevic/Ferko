package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/** A student's request to switch groups ("burza grupa"). */
public record GroupExchangeRequest(
    long id,
    long courseId,
    long studentId,
    Long fromGroupId,
    Long toGroupId,
    ExchangeStatus status,
    String reason,
    String decidedBy,
    LocalDateTime createdAt,
    LocalDateTime decidedAt) {}
