package hr.fer.zemris.ferko.infrastructure.adapter;

import java.sql.PreparedStatement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/** Small helper for inserts into identity-keyed tables that return the generated id. */
final class JdbcIds {

  private JdbcIds() {}

  static long insert(JdbcTemplate jdbcTemplate, String sql, Object... args) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbcTemplate.update(
        connection -> {
          PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
          for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
          }
          return ps;
        },
        keyHolder);
    Number key = keyHolder.getKey();
    if (key == null) {
      throw new IllegalStateException("Insert did not return a generated id: " + sql);
    }
    return key.longValue();
  }
}
