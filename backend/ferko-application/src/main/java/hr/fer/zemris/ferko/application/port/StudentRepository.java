package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Student;
import java.util.List;
import java.util.Optional;

/** Persistence port for student profiles. */
public interface StudentRepository {

  Student save(Student student);

  Optional<Student> findById(long id);

  Optional<Student> findByJmbag(String jmbag);

  Optional<Student> findByUserId(long userId);

  List<Student> findAll();
}
