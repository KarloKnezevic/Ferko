package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Room;
import java.util.List;
import java.util.Optional;

/** Persistence port for rooms. */
public interface RoomRepository {

  Room save(Room room);

  Optional<Room> findById(long id);

  Optional<Room> findByCode(String code);

  List<Room> findAll();
}
