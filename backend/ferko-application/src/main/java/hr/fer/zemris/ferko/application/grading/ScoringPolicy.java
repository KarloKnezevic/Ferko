package hr.fer.zemris.ferko.application.grading;

/**
 * Per-answer scoring policy ("način bodovanja"): points awarded for a correct, incorrect and blank
 * answer. The classic FERKO default is {@code 1 / -0.2 / 0}.
 */
public record ScoringPolicy(double correctPoints, double incorrectPoints, double blankPoints) {

  public static ScoringPolicy standard() {
    return new ScoringPolicy(1.0, -0.2, 0.0);
  }
}
