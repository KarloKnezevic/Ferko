package hr.fer.zemris.ferko.application.flag;

import java.util.Map;
import java.util.Set;

/** A {@link FlagContext} backed by plain maps/sets — handy for previews and tests. */
public record MapFlagContext(
    Set<String> presentExams, Map<String, Double> pointsByExam, Set<String> setFlags)
    implements FlagContext {

  public MapFlagContext {
    presentExams = presentExams == null ? Set.of() : Set.copyOf(presentExams);
    pointsByExam = pointsByExam == null ? Map.of() : Map.copyOf(pointsByExam);
    setFlags = setFlags == null ? Set.of() : Set.copyOf(setFlags);
  }

  @Override
  public boolean present(String examShortName) {
    return presentExams.contains(examShortName);
  }

  @Override
  public double points(String examShortName) {
    return pointsByExam.getOrDefault(examShortName, 0.0);
  }

  @Override
  public boolean flag(String flagShortName) {
    return setFlags.contains(flagShortName);
  }
}
