package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.AcademicAuditEvent;
import java.util.List;

/** Persistence port for the academic audit trail. */
public interface AuditEventRepository {

  AcademicAuditEvent save(AcademicAuditEvent event);

  /** Most recent events first, limited to {@code limit}. */
  List<AcademicAuditEvent> recent(int limit);
}
