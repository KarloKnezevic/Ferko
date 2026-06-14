package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Demonstrator;
import java.util.List;

/** Persistence port for course demonstrators. */
public interface DemonstratorRepository {

  Demonstrator save(Demonstrator demonstrator);

  List<Demonstrator> findByCourse(long courseId);

  List<Demonstrator> findByStudent(long studentId);

  boolean exists(long courseId, long studentId);

  boolean delete(long courseId, long studentId);
}
