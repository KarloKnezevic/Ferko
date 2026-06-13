package hr.fer.zemris.ferko.application.usecase.component;

import hr.fer.zemris.ferko.application.port.CourseComponentRepository;
import hr.fer.zemris.ferko.domain.model.CourseComponent;
import java.util.List;

/** Manages titled content blocks ("KOMPONENTE") shown on a course page. */
public class CourseComponentService {

  private final CourseComponentRepository repository;

  public CourseComponentService(CourseComponentRepository repository) {
    this.repository = repository;
  }

  /** Visible components for a course (what students see), ordered. */
  public List<CourseComponentView> forCourse(long courseId) {
    return repository.findByCourse(courseId).stream()
        .filter(CourseComponent::visible)
        .map(CourseComponentService::toView)
        .toList();
  }

  /** All components for a course (including hidden) — for staff management. */
  public List<CourseComponentView> allForCourse(long courseId) {
    return repository.findByCourse(courseId).stream().map(CourseComponentService::toView).toList();
  }

  public long add(long courseId, String title, String content, int ordinal, boolean visible) {
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Naslov komponente je obavezan.");
    }
    return repository
        .save(
            new CourseComponent(
                0L, courseId, title, content == null ? "" : content, ordinal, visible))
        .id();
  }

  private static CourseComponentView toView(CourseComponent component) {
    return new CourseComponentView(
        component.id(),
        component.courseId(),
        component.title(),
        component.content(),
        component.ordinal(),
        component.visible());
  }
}
