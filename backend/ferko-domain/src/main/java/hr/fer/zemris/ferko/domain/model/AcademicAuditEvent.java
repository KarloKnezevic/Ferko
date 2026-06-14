package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/** An audit-trail record of a privileged action over the academic data model. */
public record AcademicAuditEvent(
    long id,
    LocalDateTime occurredAt,
    String actor,
    String action,
    String entityType,
    String entityId,
    String details) {}
