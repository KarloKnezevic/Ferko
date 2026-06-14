package hr.fer.zemris.ferko.application.usecase.audit;

import java.time.LocalDateTime;

/** Read model for an academic audit-trail entry. */
public record AuditEventView(
    long id,
    LocalDateTime occurredAt,
    String actor,
    String action,
    String entityType,
    String entityId,
    String details) {}
