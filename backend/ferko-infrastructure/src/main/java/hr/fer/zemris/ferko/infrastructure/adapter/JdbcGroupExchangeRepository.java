package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.GroupExchangeRepository;
import hr.fer.zemris.ferko.domain.model.ExchangeStatus;
import hr.fer.zemris.ferko.domain.model.GroupExchangeRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link GroupExchangeRepository}. */
public class JdbcGroupExchangeRepository implements GroupExchangeRepository {

  private static final RowMapper<GroupExchangeRequest> MAPPER =
      (rs, rowNum) -> {
        Timestamp decidedAt = rs.getTimestamp("decided_at");
        return new GroupExchangeRequest(
            rs.getLong("id"),
            rs.getLong("course_id"),
            rs.getLong("student_id"),
            rs.getObject("from_group_id") == null ? null : rs.getLong("from_group_id"),
            rs.getObject("to_group_id") == null ? null : rs.getLong("to_group_id"),
            ExchangeStatus.valueOf(rs.getString("status")),
            rs.getString("reason"),
            rs.getString("decided_by"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            decidedAt == null ? null : decidedAt.toLocalDateTime());
      };

  private final JdbcTemplate jdbcTemplate;

  public JdbcGroupExchangeRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public GroupExchangeRequest save(GroupExchangeRequest request) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into group_exchange_request"
                + " (course_id, student_id, from_group_id, to_group_id, status, reason, created_at)"
                + " values (?, ?, ?, ?, ?, ?, ?)",
            request.courseId(),
            request.studentId(),
            request.fromGroupId(),
            request.toGroupId(),
            request.status().name(),
            request.reason(),
            request.createdAt());
    return new GroupExchangeRequest(
        id,
        request.courseId(),
        request.studentId(),
        request.fromGroupId(),
        request.toGroupId(),
        request.status(),
        request.reason(),
        request.decidedBy(),
        request.createdAt(),
        request.decidedAt());
  }

  @Override
  public List<GroupExchangeRequest> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from group_exchange_request where course_id = ? order by created_at desc",
        MAPPER,
        courseId);
  }

  @Override
  public Optional<GroupExchangeRequest> findById(long id) {
    return jdbcTemplate
        .query("select * from group_exchange_request where id = ?", MAPPER, id)
        .stream()
        .findFirst();
  }

  @Override
  public void updateDecision(
      long id, ExchangeStatus status, String decidedBy, LocalDateTime decidedAt) {
    jdbcTemplate.update(
        "update group_exchange_request set status = ?, decided_by = ?, decided_at = ? where id = ?",
        status.name(),
        decidedBy,
        decidedAt,
        id);
  }
}
