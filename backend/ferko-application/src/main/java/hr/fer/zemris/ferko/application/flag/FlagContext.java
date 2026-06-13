package hr.fer.zemris.ferko.application.flag;

/**
 * Per-student data a flag expression is evaluated against: which assessments the student has a
 * recorded result for, the points achieved, and the values of other (already computed) flags.
 */
public interface FlagContext {

  /** Whether the student has any recorded result for the assessment with the given short name. */
  boolean present(String examShortName);

  /** Points the student achieved on the given assessment (0 if none). */
  double points(String examShortName);

  /** Value of another flag by short name (false if unknown). */
  boolean flag(String flagShortName);
}
