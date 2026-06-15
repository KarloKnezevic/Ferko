package hr.fer.zemris.ferko.application.usecase.grading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.GradeThresholdRepository;
import hr.fer.zemris.ferko.application.support.InMemoryGradingRepository;
import hr.fer.zemris.ferko.domain.model.CourseGradeThresholds;
import hr.fer.zemris.ferko.domain.model.GradeComponent;
import hr.fer.zemris.ferko.domain.model.StudentPoints;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class GradeThresholdServiceTest {

  private final InMemoryGradingRepository grading = new InMemoryGradingRepository();
  private final FakeThresholds thresholds = new FakeThresholds();
  private final GradeThresholdService service =
      new GradeThresholdService(thresholds, grading, 88, 75, 62, 50);

  @Test
  void fallsBackToDefaultsThenHonoursCustom() {
    GradeThresholdService.ThresholdsView def = service.thresholdsFor(1L);
    assertEquals(88, def.excellent());
    assertFalse(def.custom());

    service.save(1L, 90, 78, 65, 55);
    GradeThresholdService.ThresholdsView custom = service.thresholdsFor(1L);
    assertEquals(90, custom.excellent());
    assertEquals(55, custom.sufficient());
    assertTrue(custom.custom());
    assertEquals(90, service.scaleFor(1L).excellent());
  }

  @Test
  void computesFinalGradesFromPointsUsingCourseScale() {
    long courseId = 7L;
    GradeComponent mi = grading.addComponent(new GradeComponent(0L, courseId, "MI", "MI", 50, 1));
    GradeComponent zi = grading.addComponent(new GradeComponent(0L, courseId, "ZI", "ZI", 50, 2));
    // Student 100: 90/100 -> 90% -> grade 5 with defaults (>=88).
    points(courseId, 100L, mi.id(), 45);
    points(courseId, 100L, zi.id(), 45);
    // Student 200: 40/100 -> 40% -> fail (grade 1).
    points(courseId, 200L, mi.id(), 20);
    points(courseId, 200L, zi.id(), 20);

    int graded = service.computeFinalGrades(courseId, "system");
    assertEquals(2, graded);
    assertEquals(5, grading.gradeFor(courseId, 100L).orElseThrow().finalGrade());
    assertEquals(1, grading.gradeFor(courseId, 200L).orElseThrow().finalGrade());
  }

  @Test
  void doesNotGradeWhenCourseHasNoComponents() {
    // Points exist but there are no components -> max total 0 -> nothing graded (no failing 1s).
    points(99L, 300L, 1L, 10);
    assertEquals(0, service.computeFinalGrades(99L, "system"));
    assertTrue(grading.gradeFor(99L, 300L).isEmpty());
  }

  private void points(long courseId, long studentId, long componentId, double pts) {
    grading.savePoints(
        new StudentPoints(
            0L, courseId, studentId, componentId, null, pts, 50, true, "t", LocalDateTime.now()));
  }

  private static final class FakeThresholds implements GradeThresholdRepository {
    private CourseGradeThresholds stored;

    @Override
    public Optional<CourseGradeThresholds> findByCourse(long courseId) {
      return Optional.ofNullable(stored).filter(t -> t.courseId() == courseId);
    }

    @Override
    public CourseGradeThresholds save(CourseGradeThresholds thresholds) {
      this.stored = thresholds;
      return thresholds;
    }
  }
}
