package hr.fer.zemris.ferko.application.grading;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Auto-grades multiple-choice answer sheets against a correct-answer key, following FERKO's "točni
 * odgovori" semantics:
 *
 * <ul>
 *   <li>a plain key like {@code "A"} requires exactly that option;
 *   <li>a comma key like {@code "A,C"} accepts any one of the listed options (alternatives);
 *   <li>a plus key like {@code "A+B"} requires exactly that set of options to be selected together.
 * </ul>
 *
 * A blank student answer scores the blank points; everything else is correct or incorrect per the
 * {@link ScoringPolicy}.
 */
public final class AutoGrader {

  private AutoGrader() {}

  public static GradedSubmission grade(
      List<String> correctKeys, List<String> studentAnswers, ScoringPolicy policy) {
    List<QuestionOutcome> outcomes = new ArrayList<>(correctKeys.size());
    double total = 0.0;
    for (int q = 0; q < correctKeys.size(); q++) {
      String answer = q < studentAnswers.size() ? studentAnswers.get(q) : "";
      QuestionOutcome outcome = gradeQuestion(correctKeys.get(q), answer);
      outcomes.add(outcome);
      total +=
          switch (outcome) {
            case CORRECT -> policy.correctPoints();
            case INCORRECT -> policy.incorrectPoints();
            case BLANK -> policy.blankPoints();
          };
    }
    return new GradedSubmission(total, outcomes);
  }

  private static QuestionOutcome gradeQuestion(String correctKey, String studentAnswer) {
    String answer = normalize(studentAnswer);
    if (answer.isEmpty()) {
      return QuestionOutcome.BLANK;
    }
    String key = normalize(correctKey);
    boolean correct;
    if (key.contains("+")) {
      Set<Character> required = letters(key.replace("+", ""));
      Set<Character> chosen = letters(answer);
      correct = required.equals(chosen);
    } else if (key.contains(",")) {
      correct = answer.length() == 1 && key.contains(answer);
    } else {
      correct = answer.equals(key);
    }
    return correct ? QuestionOutcome.CORRECT : QuestionOutcome.INCORRECT;
  }

  private static String normalize(String value) {
    return value == null ? "" : value.replaceAll("\\s", "").toUpperCase(java.util.Locale.ROOT);
  }

  private static Set<Character> letters(String value) {
    Set<Character> set = new TreeSet<>();
    for (char c : value.toCharArray()) {
      if (Character.isLetter(c)) {
        set.add(c);
      }
    }
    return set;
  }
}
