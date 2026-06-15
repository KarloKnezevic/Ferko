package hr.fer.zemris.ferko.application.usecase.grading;

/**
 * Maps a total score (as a percentage of the maximum) to a Croatian final grade 1–5 using
 * configurable thresholds. A score below the {@code sufficient} threshold fails (grade 1); the
 * passing minimum is therefore always {@code sufficient}.
 *
 * <p>Defaults follow the common FER scheme (≥88 izvrstan, ≥75 vrlo dobar, ≥62 dobar, ≥50 dovoljan),
 * but each course may carry its own thresholds.
 */
public record GradeScale(int excellent, int veryGood, int good, int sufficient) {

  public GradeScale {
    if (!(excellent > veryGood && veryGood > good && good > sufficient && sufficient > 0)) {
      throw new IllegalArgumentException(
          "thresholds must be strictly decreasing and positive: "
              + excellent
              + ">"
              + veryGood
              + ">"
              + good
              + ">"
              + sufficient
              + ">0");
    }
  }

  /** The grade (1–5) for a percentage score in [0, 100]. */
  public int gradeForPercentage(double percentage) {
    if (percentage >= excellent) {
      return 5;
    }
    if (percentage >= veryGood) {
      return 4;
    }
    if (percentage >= good) {
      return 3;
    }
    if (percentage >= sufficient) {
      return 2;
    }
    return 1;
  }

  /** The grade for a raw score against a maximum; a non-positive maximum fails. */
  public int gradeForScore(double score, double maxScore) {
    if (maxScore <= 0) {
      return 1;
    }
    return gradeForPercentage(100.0 * score / maxScore);
  }
}
