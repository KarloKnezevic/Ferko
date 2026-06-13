package hr.fer.zemris.ferko.application.grading;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class AutoGraderTest {

  private static final ScoringPolicy STANDARD = ScoringPolicy.standard();

  @Test
  void gradesSimpleAnswersWithCorrectIncorrectBlank() {
    GradedSubmission result =
        AutoGrader.grade(List.of("A", "B", "C"), List.of("A", "X", ""), STANDARD);
    // A correct (+1), B vs X incorrect (-0.2), C blank (0).
    assertEquals(0.8, result.total(), 1e-9);
    assertEquals(1, result.correct());
    assertEquals(
        List.of(QuestionOutcome.CORRECT, QuestionOutcome.INCORRECT, QuestionOutcome.BLANK),
        result.outcomes());
  }

  @Test
  void acceptsAlternativeAnswers() {
    assertEquals(1.0, AutoGrader.grade(List.of("A,C"), List.of("C"), STANDARD).total(), 1e-9);
    assertEquals(1.0, AutoGrader.grade(List.of("A,C"), List.of("A"), STANDARD).total(), 1e-9);
    assertEquals(-0.2, AutoGrader.grade(List.of("A,C"), List.of("B"), STANDARD).total(), 1e-9);
  }

  @Test
  void requiresFullSetForMandatoryAnswers() {
    assertEquals(1.0, AutoGrader.grade(List.of("A+B"), List.of("AB"), STANDARD).total(), 1e-9);
    assertEquals(1.0, AutoGrader.grade(List.of("A+B"), List.of("B A"), STANDARD).total(), 1e-9);
    assertEquals(-0.2, AutoGrader.grade(List.of("A+B"), List.of("A"), STANDARD).total(), 1e-9);
    assertEquals(-0.2, AutoGrader.grade(List.of("A+B"), List.of("ABC"), STANDARD).total(), 1e-9);
  }

  @Test
  void honoursCustomPolicyAndMissingAnswers() {
    ScoringPolicy policy = new ScoringPolicy(2.0, -1.0, 0.5);
    // Only one answer given for three questions: A correct (+2), two missing => blank (2 * 0.5).
    GradedSubmission result = AutoGrader.grade(List.of("A", "B", "C"), List.of("A"), policy);
    assertEquals(3.0, result.total(), 1e-9);
  }
}
