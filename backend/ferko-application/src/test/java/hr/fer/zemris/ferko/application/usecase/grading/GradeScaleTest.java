package hr.fer.zemris.ferko.application.usecase.grading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GradeScaleTest {

  private final GradeScale scale = new GradeScale(88, 75, 62, 50);

  @Test
  void mapsPercentagesToGrades() {
    assertEquals(5, scale.gradeForPercentage(88));
    assertEquals(5, scale.gradeForPercentage(95));
    assertEquals(4, scale.gradeForPercentage(75));
    assertEquals(4, scale.gradeForPercentage(80));
    assertEquals(3, scale.gradeForPercentage(62));
    assertEquals(2, scale.gradeForPercentage(50));
    assertEquals(1, scale.gradeForPercentage(49.9));
    assertEquals(1, scale.gradeForPercentage(0));
  }

  @Test
  void mapsRawScoreAgainstMaximum() {
    assertEquals(5, scale.gradeForScore(88, 100));
    assertEquals(2, scale.gradeForScore(25, 50));
    assertEquals(1, scale.gradeForScore(10, 0)); // non-positive max fails
  }

  @Test
  void rejectsNonMonotonicThresholds() {
    assertThrows(IllegalArgumentException.class, () -> new GradeScale(70, 75, 62, 50));
    assertThrows(IllegalArgumentException.class, () -> new GradeScale(88, 75, 62, 0));
  }
}
