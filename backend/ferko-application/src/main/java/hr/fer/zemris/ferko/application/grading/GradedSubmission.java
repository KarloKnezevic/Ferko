package hr.fer.zemris.ferko.application.grading;

import java.util.List;

/** Result of auto-grading one submission: total points and the per-question outcomes. */
public record GradedSubmission(double total, List<QuestionOutcome> outcomes) {

  public GradedSubmission {
    outcomes = List.copyOf(outcomes);
  }

  public long correct() {
    return outcomes.stream().filter(o -> o == QuestionOutcome.CORRECT).count();
  }
}
