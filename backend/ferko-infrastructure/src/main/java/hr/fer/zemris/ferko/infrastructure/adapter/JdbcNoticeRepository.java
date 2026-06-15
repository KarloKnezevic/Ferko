package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.NoticeRepository;
import hr.fer.zemris.ferko.domain.model.Notice;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link NoticeRepository}. */
public class JdbcNoticeRepository implements NoticeRepository {

  private static final RowMapper<Notice> MAPPER =
      (rs, rowNum) ->
          new Notice(
              rs.getLong("id"),
              rs.getObject("course_id") == null ? null : rs.getLong("course_id"),
              rs.getString("title"),
              rs.getString("body"),
              rs.getString("author_name"),
              rs.getTimestamp("created_at").toLocalDateTime(),
              rs.getBoolean("pinned"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcNoticeRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Notice save(Notice notice) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into obavijest (course_id, title, body, author_name, created_at, pinned)"
                + " values (?, ?, ?, ?, ?, ?)",
            notice.courseId(),
            notice.title(),
            notice.body(),
            notice.authorName(),
            notice.createdAt(),
            notice.pinned());
    return new Notice(
        id,
        notice.courseId(),
        notice.title(),
        notice.body(),
        notice.authorName(),
        notice.createdAt(),
        notice.pinned());
  }

  @Override
  public List<Notice> findRecent(int limit) {
    return jdbcTemplate.query(
        "select * from obavijest order by pinned desc, created_at desc limit ?", MAPPER, limit);
  }

  @Override
  public List<Notice> findByCourse(long courseId) {
    return jdbcTemplate.query(
        "select * from obavijest where course_id = ? order by pinned desc, created_at desc",
        MAPPER,
        courseId);
  }

  @Override
  public Optional<Notice> findById(long id) {
    return jdbcTemplate.query("select * from obavijest where id = ?", MAPPER, id).stream()
        .findFirst();
  }

  @Override
  public boolean deleteById(long id) {
    return jdbcTemplate.update("delete from obavijest where id = ?", id) > 0;
  }
}
