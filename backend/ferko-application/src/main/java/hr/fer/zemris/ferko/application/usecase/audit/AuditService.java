package hr.fer.zemris.ferko.application.usecase.audit;

import hr.fer.zemris.ferko.application.port.AuditEventRepository;
import hr.fer.zemris.ferko.domain.model.AcademicAuditEvent;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.time.LocalDateTime;
import java.util.List;

/** Records and reads the academic audit trail of privileged actions. */
public class AuditService {

  private static final int MAX_LIMIT = 500;
  private static final Logger LOG = System.getLogger(AuditService.class.getName());

  private final AuditEventRepository repository;

  public AuditService(AuditEventRepository repository) {
    this.repository = repository;
  }

  /**
   * Records an audited action. Best-effort: a failure to persist the audit entry is logged and
   * swallowed so it never breaks the privileged action that triggered it.
   */
  public void record(
      String actor, String action, String entityType, String entityId, String details) {
    try {
      repository.save(
          new AcademicAuditEvent(
              0L,
              LocalDateTime.now(),
              clamp(actor == null || actor.isBlank() ? "unknown" : actor, 128),
              clamp(action, 64),
              clamp(entityType, 64),
              clamp(entityId, 64),
              details == null ? "" : details));
    } catch (RuntimeException ex) {
      LOG.log(Level.WARNING, () -> "Failed to record audit event: " + action);
    }
  }

  private static String clamp(String value, int max) {
    if (value == null) {
      return null;
    }
    return value.length() <= max ? value : value.substring(0, max);
  }

  public List<AuditEventView> recent(int limit) {
    int capped = limit <= 0 ? 50 : Math.min(limit, MAX_LIMIT);
    return repository.recent(capped).stream().map(AuditService::toView).toList();
  }

  private static AuditEventView toView(AcademicAuditEvent event) {
    return new AuditEventView(
        event.id(),
        event.occurredAt(),
        event.actor(),
        event.action(),
        event.entityType(),
        event.entityId(),
        event.details());
  }
}
