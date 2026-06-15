package hr.fer.zemris.ferko.application.usecase.timetable;

import java.util.List;

/** View DTOs for the interactive collision-resolution workbench. */
public final class ScheduleResolutionViews {

  private ScheduleResolutionViews() {}

  /**
   * Faculty-wide hard-constraint report. A conflict-free timetable has all four counters at zero.
   *
   * <p>{@code totalCollisions} is the grand total ({@code room + instructor + group + capacity})
   * and equals the sum of every {@link HeatCell#count()} in {@code heatmap}. The {@code heatmap} is
   * aggregated over <em>all</em> collisions (never capped), so a UI built from it always sums back
   * to the per-kind counters. {@code collisions} is the capped detailed list (with move
   * suggestions) used for click-through; it may hold fewer entries than {@code totalCollisions}.
   */
  public record ResolutionReportView(
      int totalSlots,
      int roomCollisions,
      int instructorCollisions,
      int groupCollisions,
      int capacityViolations,
      int totalCollisions,
      boolean conflictFree,
      List<HeatCell> heatmap,
      List<CollisionView> collisions) {}

  /**
   * One aggregated heatmap bucket: the number of collisions of a single {@code kind} ({@code ROOM},
   * {@code INSTRUCTOR}, {@code GROUP} or {@code CAPACITY}) occurring in {@code room} on a given
   * {@code dayOfWeek}. Computed over every collision, not just the detailed (capped) subset.
   */
  public record HeatCell(String room, String dayOfWeek, String kind, int count) {}

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

  /**
   * One ranked free-slot ("gap") a session can be moved into without breaking any hard constraint.
   * {@code score} is a soft-constraint penalty (lower is better — the list is returned best-first);
   * {@code reasons} explains the trade-offs (minimal disruption, day load, capacity fit, group
   * spread) so the admin can choose deliberately. {@code current} marks the gap that keeps the
   * session's present weekday/time (room change only).
   */
  public record CandidateView(
      String dayOfWeek,
      String startsAt,
      String endsAt,
      Long roomId,
      String roomCode,
      int freeSeats,
      double score,
      boolean current,
      List<String> reasons) {}
}
