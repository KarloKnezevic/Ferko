package hr.fer.zemris.ferko.application.flag;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FlagExpressionTest {

  private static FlagContext context() {
    return new MapFlagContext(
        Set.of("MI1", "LAB"), Map.of("MI1", 14.0, "ZI", 5.0), Set.of("UVJET"));
  }

  @Test
  void blankExpressionAlwaysQualifies() {
    assertTrue(FlagExpression.evaluate(null, context()));
    assertTrue(FlagExpression.evaluate("   ", context()));
  }

  @Test
  void presentAndNegation() {
    assertTrue(FlagExpression.evaluate("present(\"MI1\")", context()));
    assertFalse(FlagExpression.evaluate("present(\"ZI2\")", context()));
    assertTrue(FlagExpression.evaluate("!present(\"ZI2\")", context()));
  }

  @Test
  void booleanComposition() {
    assertTrue(FlagExpression.evaluate("present(\"MI1\") && present(\"LAB\")", context()));
    assertFalse(FlagExpression.evaluate("present(\"MI1\") && present(\"X\")", context()));
    assertTrue(FlagExpression.evaluate("present(\"X\") || present(\"LAB\")", context()));
    assertTrue(
        FlagExpression.evaluate(
            "(present(\"X\") || present(\"MI1\")) && !present(\"Y\")", context()));
  }

  @Test
  void numericComparisonsOnPoints() {
    assertTrue(FlagExpression.evaluate("points(\"MI1\") >= 10", context()));
    assertFalse(FlagExpression.evaluate("points(\"MI1\") < 10", context()));
    assertTrue(FlagExpression.evaluate("points(\"ZI\") == 5", context()));
    assertTrue(FlagExpression.evaluate("points(\"NEMA\") <= 0", context()));
    assertTrue(FlagExpression.evaluate("points(\"MI1\") >= 10 && points(\"ZI\") != 0", context()));
  }

  @Test
  void flagReferenceAndLiterals() {
    assertTrue(FlagExpression.evaluate("flag(\"UVJET\")", context()));
    assertFalse(FlagExpression.evaluate("flag(\"DRUGI\")", context()));
    assertTrue(FlagExpression.evaluate("true", context()));
    assertFalse(FlagExpression.evaluate("false && true", context()));
  }

  @Test
  void rejectsMalformedExpressions() {
    assertThrows(
        FlagExpressionException.class, () -> FlagExpression.evaluate("present(\"X\"", context()));
    assertThrows(
        FlagExpressionException.class, () -> FlagExpression.evaluate("bogus(\"X\")", context()));
    assertThrows(
        FlagExpressionException.class,
        () -> FlagExpression.evaluate("present(\"X\") &&", context()));
    assertThrows(FlagExpressionException.class, () -> FlagExpression.evaluate("@", context()));
    assertThrows(
        FlagExpressionException.class, () -> FlagExpression.evaluate("points(\"X\")", context()));
  }
}
