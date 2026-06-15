package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.CourseGradeThresholds;
import java.util.Optional;

/** Persistence port for per-course grade thresholds. */
public interface GradeThresholdRepository {

  Optional<CourseGradeThresholds> findByCourse(long courseId);

  /** Inserts or updates the thresholds for a course. */
  CourseGradeThresholds save(CourseGradeThresholds thresholds);
}
