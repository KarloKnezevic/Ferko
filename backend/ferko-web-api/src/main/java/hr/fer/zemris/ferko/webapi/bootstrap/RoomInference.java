package hr.fer.zemris.ferko.webapi.bootstrap;

import java.util.Locale;

/**
 * Infers room attributes (building, capacity, invigilator need, computer equipment) from a bare
 * room code found in the legacy timetables. The legacy data only carries room codes, so capacities
 * are reasonable demonstration estimates rather than authoritative figures: computer classrooms
 * (e.g. A101–A210, PCLAB1–3) are small, lecture halls are large.
 */
final class RoomInference {

  private RoomInference() {}

  /** Inferred room attributes. */
  record RoomSpec(String building, int capacity, int requiredAssistants, boolean hasComputers) {}

  static RoomSpec infer(String rawCode) {
    String code = rawCode == null ? "" : rawCode.trim().toUpperCase(Locale.ROOT);
    if (isComputerClassroom(code)) {
      return new RoomSpec(building(code), 20, 1, true);
    }
    int capacity = isLargeHall(code) ? 150 : 80;
    return new RoomSpec(building(code), capacity, 2, false);
  }

  private static boolean isComputerClassroom(String code) {
    return code.startsWith("PCLAB")
        || code.contains("PRAKTIKUM")
        || code.contains("RACUNAL")
        // A101–A110, A209–A210 etc. are the faculty computer classrooms.
        || code.matches("A[12]\\d{2}");
  }

  private static boolean isLargeHall(String code) {
    // Single-letter prefix followed by a short number is typically a large lecture hall (e.g. D272,
    // B2, A101). Treat the very biggest known halls generously.
    return code.matches("[A-Z]\\d{1,3}") || code.matches("[A-Z]\\d{1,3}[A-Z]?");
  }

  private static String building(String code) {
    if (code.isEmpty()) {
      return "FER";
    }
    return switch (code.charAt(0)) {
      case 'A' -> "Siva zgrada (A)";
      case 'B' -> "Bijela zgrada (B)";
      case 'C' -> "C zgrada";
      case 'D' -> "D zgrada";
      default -> "FER";
    };
  }
}
