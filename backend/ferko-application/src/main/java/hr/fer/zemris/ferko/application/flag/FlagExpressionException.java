package hr.fer.zemris.ferko.application.flag;

/** Raised when a flag expression cannot be parsed or evaluated. */
public class FlagExpressionException extends RuntimeException {

  public FlagExpressionException(String message) {
    super(message);
  }
}
