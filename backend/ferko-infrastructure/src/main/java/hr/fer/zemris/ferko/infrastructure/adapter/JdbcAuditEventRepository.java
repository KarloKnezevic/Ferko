package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.AuditEventRepository;
import hr.fer.zemris.ferko.domain.model.AcademicAuditEvent;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link AuditEventRepository} over {@code academic_audit_event}. */
public class JdbcAuditEventRepository implements AuditEventRepository {

  private static final RowMapper<AcademicAuditEvent> MAPPER =
      (rs, rowNum) ->
          new AcademicAuditEvent(
              rs.getLong("id"),
              rs.getTimestamp("occurred_at").toLocalDateTime(),
              rs.getString("actor"),
              rs.getString("action"),
              rs.getString("entity_type"),
              rs.getString("entity_id"),
              rs.getString("details"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcAuditEventRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public AcademicAuditEvent save(AcademicAuditEvent event) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into academic_audit_event"
                + " (occurred_at, actor, action, entity_type, entity_id, details)"
                + " values (?, ?, ?, ?, ?, ?)",
            Timestamp.valueOf(event.occurredAt()),
            event.actor(),
            event.action(),
            event.entityType(),
            event.entityId(),
            event.details());
    return new AcademicAuditEvent(
        id,
        event.occurredAt(),
        event.actor(),
        event.action(),
        event.entityType(),
        event.entityId(),
        event.details());
  }

  @Override
  public List<AcademicAuditEvent> recent(int limit) {
    return jdbcTemplate.query(
        "select * from academic_audit_event order by occurred_at desc, id desc limit ?",
        MAPPER,
        limit);
  }
}
