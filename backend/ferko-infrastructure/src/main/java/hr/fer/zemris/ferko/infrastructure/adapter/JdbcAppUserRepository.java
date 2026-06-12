package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Role;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link AppUserRepository}. */
public class JdbcAppUserRepository implements AppUserRepository {

  private final JdbcTemplate jdbcTemplate;

  public JdbcAppUserRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public AppUser save(AppUser user) {
    long id = user.id();
    if (id <= 0) {
      id =
          JdbcIds.insert(
              jdbcTemplate,
              "insert into app_user (username, password_hash, full_name, email, active, created_at)"
                  + " values (?, ?, ?, ?, ?, ?)",
              user.username(),
              user.passwordHash(),
              user.fullName(),
              user.email(),
              user.active(),
              Timestamp.valueOf(user.createdAt()));
    } else {
      jdbcTemplate.update(
          "update app_user set username = ?, password_hash = ?, full_name = ?, email = ?,"
              + " active = ? where id = ?",
          user.username(),
          user.passwordHash(),
          user.fullName(),
          user.email(),
          user.active(),
          id);
    }
    jdbcTemplate.update("delete from user_role where user_id = ?", id);
    for (Role role : user.roles()) {
      jdbcTemplate.update("insert into user_role (user_id, role) values (?, ?)", id, role.name());
    }
    return findById(id).orElseThrow();
  }

  @Override
  public Optional<AppUser> findById(long id) {
    return jdbcTemplate.query("select * from app_user where id = ?", mapper(), id).stream()
        .findFirst();
  }

  @Override
  public Optional<AppUser> findByUsername(String username) {
    return jdbcTemplate
        .query("select * from app_user where username = ?", mapper(), username)
        .stream()
        .findFirst();
  }

  @Override
  public List<AppUser> findAll() {
    return jdbcTemplate.query("select * from app_user order by id", mapper());
  }

  private RowMapper<AppUser> mapper() {
    return (ResultSet rs, int rowNum) -> {
      long userId = rs.getLong("id");
      Set<Role> roles = loadRoles(userId);
      Timestamp createdAt = rs.getTimestamp("created_at");
      return new AppUser(
          userId,
          rs.getString("username"),
          rs.getString("password_hash"),
          rs.getString("full_name"),
          rs.getString("email"),
          rs.getBoolean("active"),
          createdAt == null ? null : createdAt.toLocalDateTime(),
          roles);
    };
  }

  private Set<Role> loadRoles(long userId) {
    List<Role> roles =
        jdbcTemplate.query(
            "select role from user_role where user_id = ?",
            (RowMapper<Role>) (rs, rowNum) -> Role.valueOf(rs.getString("role")),
            userId);
    return roles.isEmpty() ? EnumSet.noneOf(Role.class) : EnumSet.copyOf(roles);
  }
}
