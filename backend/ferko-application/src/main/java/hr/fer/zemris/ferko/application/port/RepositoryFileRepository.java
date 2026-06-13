package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.RepositoryFile;
import java.util.List;
import java.util.Optional;

/** Persistence port for course repository file metadata. */
public interface RepositoryFileRepository {

  RepositoryFile save(RepositoryFile file);

  List<RepositoryFile> findByCourse(long courseId);

  Optional<RepositoryFile> findById(long id);
}
