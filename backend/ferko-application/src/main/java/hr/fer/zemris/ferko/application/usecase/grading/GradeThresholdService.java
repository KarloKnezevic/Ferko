package hr.fer.zemris.ferko.application.usecase.grading;

import hr.fer.zemris.ferko.application.port.GradeThresholdRepository;
import hr.fer.zemris.ferko.application.port.GradingRepository;
import hr.fer.zemris.ferko.domain.model.CourseGradeThresholds;
import hr.fer.zemris.ferko.domain.model.Grade;
import hr.fer.zemris.ferko.domain.model.GradeComponent;
import hr.fer.zemris.ferko.domain.model.StudentPoints;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages per-course grade thresholds and the recomputation of final grades from points. When a
 * course has no custom thresholds the configured global defaults apply.
 */
public class GradeThresholdService {

  private final GradeThresholdRepository thresholdRepository;
  private final GradingRepository gradingRepository;
  private final int defaultExcellent;
  private final int defaultVeryGood;
  private final int defaultGood;
  private final int defaultSufficient;

  public GradeThresholdService(
      GradeThresholdRepository thresholdRepository,
      GradingRepository gradingRepository,
      int defaultExcellent,
      int defaultVeryGood,
      int defaultGood,
      int defaultSufficient) {
    this.thresholdRepository = thresholdRepository;
    this.gradingRepository = gradingRepository;
    this.defaultExcellent = defaultExcellent;
    this.defaultVeryGood = defaultVeryGood;
    this.defaultGood = defaultGood;
    this.defaultSufficient = defaultSufficient;
  }

  /**
   * The thresholds in effect for a course, flagging whether they are course-specific or default.
   */
  public ThresholdsView thresholdsFor(long courseId) {
    return thresholdRepository
        .findByCourse(courseId)
        .map(t -> new ThresholdsView(t.excellent(), t.veryGood(), t.good(), t.sufficient(), true))
        .orElseGet(
            () ->
                new ThresholdsView(
                    defaultExcellent, defaultVeryGood, defaultGood, defaultSufficient, false));
  }

  /** The grade scale in effect for a course (custom thresholds or defaults). */
  public GradeScale scaleFor(long courseId) {
    ThresholdsView view = thresholdsFor(courseId);
    return new GradeScale(view.excellent(), view.veryGood(), view.good(), view.sufficient());
  }

  /** Saves (and validates) course-specific thresholds. */
  public void save(long courseId, int excellent, int veryGood, int good, int sufficient) {
    // Construct a GradeScale first so the strictly-decreasing/positive invariant is enforced.
    new GradeScale(excellent, veryGood, good, sufficient);
    thresholdRepository.save(
        new CourseGradeThresholds(courseId, excellent, veryGood, good, sufficient));
  }

  /**
   * Recomputes every student's final grade for a course from their points using the course scale.
   * Returns the number of grades written.
   */
  public int computeFinalGrades(long courseId, String decidedBy) {
    double maxTotal =
        gradingRepository.componentsByCourse(courseId).stream()
            .mapToDouble(GradeComponent::maxPoints)
            .sum();
    if (maxTotal <= 0) {
      // No gradeable components yet — do not write failing grades to everyone.
      return 0;
    }
    Map<Long, Double> totalByStudent = new LinkedHashMap<>();
    for (StudentPoints points : gradingRepository.pointsByCourse(courseId)) {
      if (points.componentId() != null) {
        totalByStudent.merge(points.studentId(), points.points(), Double::sum);
      }
    }
    GradeScale scale = scaleFor(courseId);
    int written = 0;
    for (Map.Entry<Long, Double> entry : totalByStudent.entrySet()) {
      int grade = scale.gradeForScore(entry.getValue(), maxTotal);
      gradingRepository.saveGrade(
          new Grade(
              0L,
              courseId,
              entry.getKey(),
              grade,
              entry.getValue(),
              decidedBy,
              LocalDateTime.now()));
      written++;
    }
    return written;
  }

  /** Effective thresholds plus whether they override the defaults. */
  public record ThresholdsView(
      int excellent, int veryGood, int good, int sufficient, boolean custom) {}
}
