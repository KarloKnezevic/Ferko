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
      int totalSlots, int roomConflicts, int instructorConflicts, List<ConflictView> conflicts) {}

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
