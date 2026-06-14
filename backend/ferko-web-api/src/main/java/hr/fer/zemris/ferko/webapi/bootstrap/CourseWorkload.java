package hr.fer.zemris.ferko.webapi.bootstrap;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses the legacy {@code opterecenja} field, a newline-separated list of weekly hour counts
 * followed by the ECTS value, e.g. {@code "2\n1\n1\n0\n4.0"} means 2h lectures, 1h auditory
 * exercises, 1h laboratory, 0h other and 4 ECTS. The final token is always the ECTS credit value.
 */
final class CourseWorkload {

  private final List<Integer> weeklyHours;
  private final int ects;

  private CourseWorkload(List<Integer> weeklyHours, int ects) {
    this.weeklyHours = weeklyHours;
    this.ects = ects;
  }

  static CourseWorkload parse(String raw, int fallbackEcts) {
    if (raw == null || raw.isBlank()) {
      return new CourseWorkload(List.of(), fallbackEcts);
    }
    String[] tokens = raw.trim().split("\\R");
    List<Integer> hours = new ArrayList<>();
    int ects = fallbackEcts;
    for (int i = 0; i < tokens.length; i++) {
      Double value = parseNumber(tokens[i]);
      if (value == null) {
        continue;
      }
      if (i == tokens.length - 1) {
        // The last numeric token is the ECTS credit value (e.g. 4.0 -> 4, 7.5 -> 8).
        int rounded = (int) Math.round(value);
        ects = rounded >= 1 && rounded <= 30 ? rounded : fallbackEcts;
      } else {
        hours.add((int) Math.round(value));
      }
    }
    return new CourseWorkload(List.copyOf(hours), ects);
  }

  int ects() {
    return ects;
  }

  /** Sum of weekly contact hours (lectures + exercises + lab + …). */
  int weeklyContactHours() {
    return weeklyHours.stream().mapToInt(Integer::intValue).sum();
  }

  /** Compact "P+A+L+…" summary of weekly hours, or empty when unknown. */
  String hoursSummary() {
    if (weeklyHours.isEmpty()) {
      return "";
    }
    List<String> parts = new ArrayList<>();
    for (int hour : weeklyHours) {
      parts.add(Integer.toString(hour));
    }
    return String.join("+", parts);
  }

  private static Double parseNumber(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    try {
      return Double.parseDouble(token.trim().replace(',', '.'));
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
