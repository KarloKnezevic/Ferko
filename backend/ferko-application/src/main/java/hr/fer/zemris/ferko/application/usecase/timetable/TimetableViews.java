package hr.fer.zemris.ferko.application.usecase.timetable;

import java.util.List;

/** View DTOs for the weekly teaching timetable and its collision report. */
public final class TimetableViews {

  private TimetableViews() {}

  /** A single weekly timetable slot, ready for display. */
  public record TimetableSlotView(
      long courseId,
      String courseCode,
      String courseName,
      String type,
      String dayOfWeek,
      String startsAt,
      String endsAt,
      String room,
      String instructor) {}

  /**
   * Summary of timetable collisions. A collision is two slots overlapping in time on the same day
   * that share a constrained resource (the same room, or the same instructor) across different
   * courses.
   */
  public record CollisionReportView(
      int totalSlots,
      int roomConflicts,
      int instructorConflicts,
      List<ConflictView> conflicts,
      List<RoomUsageView> roomUtilization,
      List<OverCapacityView> overCapacity,
      List<RoomHeatView> heatmap) {}

  /** Weekly slot count for a room (busiest rooms first). */
  public record RoomUsageView(String room, int slots) {}

  /** A scheduled slot whose enrolled course exceeds the assigned room's capacity. */
  public record OverCapacityView(
      String courseCode,
      String courseName,
      String room,
      String dayOfWeek,
      String startsAt,
      String endsAt,
      int enrolled,
      int capacity) {}

  /**
   * Heatmap row for one room: weekly slot counts per weekday (Monday..Friday) for shading, the room
   * capacity, the weekly total and whether any slot in the room is over capacity.
   */
  public record RoomHeatView(
      String room, int capacity, List<Integer> perDay, int total, boolean overCapacity) {}

  /** A single detected conflict. {@code kind} is {@code ROOM} or {@code INSTRUCTOR}. */
  public record ConflictView(
      String kind,
      String resource,
      String dayOfWeek,
      String startsAt,
      String endsAt,
      String courseA,
      String courseB) {}
}
