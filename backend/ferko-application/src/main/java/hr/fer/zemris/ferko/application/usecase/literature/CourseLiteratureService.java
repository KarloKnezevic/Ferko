package hr.fer.zemris.ferko.application.usecase.literature;

import hr.fer.zemris.ferko.application.port.CourseLiteratureRepository;
import hr.fer.zemris.ferko.domain.model.CourseLiterature;
import java.util.List;

/** Manages a course reading list ("Literatura"): required and recommended entries. */
public class CourseLiteratureService {

  private final CourseLiteratureRepository repository;

  public CourseLiteratureService(CourseLiteratureRepository repository) {
    this.repository = repository;
  }

  public List<CourseLiteratureView> forCourse(long courseId) {
    return repository.findByCourse(courseId).stream().map(CourseLiteratureService::toView).toList();
  }

  public long add(long courseId, String title, String author, boolean mandatory, int ordinal) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Naslov literature je obavezan.");
    }
    return repository
        .save(
            new CourseLiterature(
                0L,
                courseId,
                title.trim(),
                author == null ? "" : author.trim(),
                mandatory,
                ordinal))
        .id();
  }

  private static CourseLiteratureView toView(CourseLiterature literature) {
    return new CourseLiteratureView(
        literature.id(),
        literature.courseId(),
        literature.title(),
        literature.author(),
        literature.mandatory(),
        literature.ordinal());
  }
}
