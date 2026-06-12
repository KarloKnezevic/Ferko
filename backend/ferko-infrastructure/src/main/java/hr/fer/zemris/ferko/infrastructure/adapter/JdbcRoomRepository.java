package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.domain.model.Room;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/** JDBC adapter for {@link RoomRepository}. */
public class JdbcRoomRepository implements RoomRepository {

  private static final RowMapper<Room> MAPPER =
      (rs, rowNum) ->
          new Room(
              rs.getLong("id"),
              rs.getString("code"),
              rs.getString("building"),
              rs.getInt("capacity"),
              rs.getInt("required_assistants"),
              rs.getBoolean("has_computers"));

  private final JdbcTemplate jdbcTemplate;

  public JdbcRoomRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public Room save(Room room) {
    long id = room.id();
    if (id <= 0) {
      id =
          JdbcIds.insert(
              jdbcTemplate,
              "insert into room (code, building, capacity, required_assistants, has_computers)"
                  + " values (?, ?, ?, ?, ?)",
              room.code(),
              room.building(),
              room.capacity(),
              room.requiredAssistants(),
              room.hasComputers());
    } else {
      jdbcTemplate.update(
          "update room set code = ?, building = ?, capacity = ?, required_assistants = ?,"
              + " has_computers = ? where id = ?",
          room.code(),
          room.building(),
          room.capacity(),
          room.requiredAssistants(),
          room.hasComputers(),
          id);
    }
    return findById(id).orElseThrow();
  }

  @Override
  public Optional<Room> findById(long id) {
    return jdbcTemplate.query("select * from room where id = ?", MAPPER, id).stream().findFirst();
  }

  @Override
  public Optional<Room> findByCode(String code) {
    return jdbcTemplate.query("select * from room where code = ?", MAPPER, code).stream()
        .findFirst();
  }

  @Override
  public List<Room> findAll() {
    return jdbcTemplate.query("select * from room order by code", MAPPER);
  }
}
