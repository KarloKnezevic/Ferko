package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.CourseLiterature;
import java.util.List;

/** Persistence port for course reading lists ("Literatura"). */
public interface CourseLiteratureRepository {

  CourseLiterature save(CourseLiterature literature);

  /** Reading-list entries for a course, ordered by {@code ordinal} then id. */
  List<CourseLiterature> findByCourse(long courseId);
}
