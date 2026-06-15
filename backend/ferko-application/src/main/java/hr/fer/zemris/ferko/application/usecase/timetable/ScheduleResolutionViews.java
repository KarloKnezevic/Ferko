package hr.fer.zemris.ferko.application.usecase.timetable;

import java.util.List;

/** View DTOs for the interactive collision-resolution workbench. */
public final class ScheduleResolutionViews {

  private ScheduleResolutionViews() {}

  /**
   * Faculty-wide hard-constraint report. A conflict-free timetable has all four counters at zero.
   */
  public record ResolutionReportView(
      int totalSlots,
      int roomCollisions,
      int instructorCollisions,
      int groupCollisions,
      int capacityViolations,
      boolean conflictFree,
      List<CollisionView> collisions) {}

  /**
   * One hard-constraint violation. {@code kind} is {@code ROOM}, {@code INSTRUCTOR}, {@code GROUP}
   * or {@code CAPACITY}. {@code slotId} is the session a fix would move; {@code otherSlotId} is the
   * conflicting partner ({@code null} for a capacity violation).
   */
  public record CollisionView(
      String kind,
      String dayOfWeek,
      String startsAt,
      String endsAt,
      String resource,
      String room,
      long slotId,
      String slotLabel,
      Long otherSlotId,
      String otherLabel,
      MoveSuggestionView suggestion) {}

  /**
   * A proposed conflict-free placement for {@code slotId}; {@code feasible=false} when none found.
   */
  public record MoveSuggestionView(
      boolean feasible,
      String dayOfWeek,
      String startsAt,
      String endsAt,
      Long roomId,
      String roomCode,
      String note) {}
}
