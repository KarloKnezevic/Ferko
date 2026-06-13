package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.flag.FlagExpression;
import hr.fer.zemris.ferko.application.flag.FlagExpressionException;
import hr.fer.zemris.ferko.application.flag.MapFlagContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Evaluates assessment-flag ("zastavica") expressions against a sample student context, powering
 * the flag-program editor preview without executing arbitrary code.
 */
@RestController
@RequestMapping("/api/v1/academic/flags")
public class FlagController {

  @PostMapping("/evaluate")
  @PreAuthorize("hasAnyRole('ADMIN', 'NOSITELJ', 'ASISTENT_ORGANIZATOR')")
  public EvaluateFlagResponse evaluate(@RequestBody EvaluateFlagRequest request) {
    MapFlagContext context =
        new MapFlagContext(
            toSet(request.presentExams()), request.points(), toSet(request.setFlags()));
    try {
      return new EvaluateFlagResponse(FlagExpression.evaluate(request.expression(), context));
    } catch (FlagExpressionException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
  }

  private static Set<String> toSet(List<String> values) {
    return values == null ? Set.of() : Set.copyOf(values);
  }

  /** Sample student context + the flag program to evaluate. */
  public record EvaluateFlagRequest(
      String expression,
      List<String> presentExams,
      Map<String, Double> points,
      List<String> setFlags) {}

  /** Computed flag value. */
  public record EvaluateFlagResponse(boolean value) {}
}
