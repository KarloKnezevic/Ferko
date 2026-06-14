package hr.fer.zemris.ferko.webapi.bootstrap;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helpers for turning the legacy {@code nositelji}/{@code izvodaci} free-text fields (one name per
 * line, often prefixed with academic titles such as "Prof. dr. sc.") into clean display names and
 * deterministic usernames for seeded staff users.
 */
final class StaffNames {

  private StaffNames() {}

  /** Splits a multi-line names field into individual, title-stripped display names. */
  static List<String> parseNames(String raw) {
    List<String> names = new ArrayList<>();
    if (raw == null || raw.isBlank()) {
      return names;
    }
    for (String line : raw.split("\\R")) {
      String name = stripTitles(line);
      if (!name.isBlank()) {
        names.add(name);
      }
    }
    return names;
  }

  /** Removes academic-title tokens (anything containing a dot, e.g. "Prof.", "dr.", "sc."). */
  static String stripTitles(String raw) {
    if (raw == null) {
      return "";
    }
    List<String> kept = new ArrayList<>();
    for (String token : raw.trim().split("\\s+")) {
      if (!token.isBlank() && !token.contains(".")) {
        kept.add(token);
      }
    }
    return String.join(" ", kept).trim();
  }

  /**
   * Builds a deterministic, ASCII username from a display name, e.g. "Davor Petrinović" →
   * "davor.petrinovic". Returns an empty string when no usable name is present.
   */
  static String toUsername(String displayName) {
    String ascii = transliterate(displayName).toLowerCase(Locale.ROOT);
    List<String> parts = new ArrayList<>();
    for (String token : ascii.split("\\s+")) {
      String cleaned = token.replaceAll("[^a-z0-9]", "");
      if (!cleaned.isBlank()) {
        parts.add(cleaned);
      }
    }
    return String.join(".", parts);
  }

  /** Replaces Croatian diacritics and strips remaining combining marks. */
  static String transliterate(String input) {
    if (input == null) {
      return "";
    }
    String replaced =
        input
            .replace("đ", "d")
            .replace("Đ", "D")
            .replace("ž", "z")
            .replace("Ž", "Z")
            .replace("š", "s")
            .replace("Š", "S")
            .replace("č", "c")
            .replace("Č", "C")
            .replace("ć", "c")
            .replace("Ć", "C");
    return Normalizer.normalize(replaced, Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
  }
}
