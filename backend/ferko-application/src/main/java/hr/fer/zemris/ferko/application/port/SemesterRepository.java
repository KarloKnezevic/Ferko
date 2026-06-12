package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Semester;
import java.util.List;
import java.util.Optional;

/** Persistence port for academic semesters. */
public interface SemesterRepository {

  Semester save(Semester semester);

  Optional<Semester> findByCode(String code);

  Optional<Semester> findActive();

  List<Semester> findAll();
}
