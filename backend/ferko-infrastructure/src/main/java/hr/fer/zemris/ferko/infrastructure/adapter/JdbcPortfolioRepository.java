package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.PortfolioRepository;
import hr.fer.zemris.ferko.domain.model.PortfolioEntry;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link PortfolioRepository}. */
public class JdbcPortfolioRepository implements PortfolioRepository {

  private static final RowMapper<PortfolioEntry> MAPPER =
      (rs, rowNum) ->
          new PortfolioEntry(
              rs.getLong("id"),
              rs.getLong("user_id"),
              rs.getString("title"),
              rs.getString("description"),
              rs.getString("category"),
              rs.getString("link"),
              rs.getTimestamp("created_at").toLocalDateTime());

  private final JdbcTemplate jdbcTemplate;

  public JdbcPortfolioRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public PortfolioEntry save(PortfolioEntry entry) {
    long id =
        JdbcIds.insert(
            jdbcTemplate,
            "insert into eportfolio_unos (user_id, title, description, category, link, created_at)"
                + " values (?, ?, ?, ?, ?, ?)",
            entry.userId(),
            entry.title(),
            entry.description(),
            entry.category(),
            entry.link(),
            Timestamp.valueOf(entry.createdAt()));
    return new PortfolioEntry(
        id,
        entry.userId(),
        entry.title(),
        entry.description(),
        entry.category(),
        entry.link(),
        entry.createdAt());
  }

  @Override
  public List<PortfolioEntry> findByUser(long userId) {
    return jdbcTemplate.query(
        "select * from eportfolio_unos where user_id = ? order by created_at desc, id desc",
        MAPPER,
        userId);
  }

  @Override
  public void remove(long userId, long entryId) {
    jdbcTemplate.update(
        "delete from eportfolio_unos where id = ? and user_id = ?", entryId, userId);
  }
}
