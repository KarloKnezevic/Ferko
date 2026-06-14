package hr.fer.zemris.ferko.application.usecase.timetable;

import java.util.List;

/** View DTOs for engine-generated lecture timetables. */
public final class LectureTimetablingViews {

  private LectureTimetablingViews() {}

  /**
   * Result of one generation run. {@code baselineConflicts} is the number of student-sharing course
   * pairs (the conflicts that would remain if every course shared a slot); {@code resultConflicts}
   * is how many remain in the generated assignment — the engine's achievement is the reduction.
   */
  public record GeneratedTimetableView(
      String algorithm,
      int periods,
      int courses,
      int baselineConflicts,
      int resultConflicts,
      boolean feasible,
      int iterations,
      List<Double> convergence,
      List<CourseAssignmentView> assignments) {}

  /** A course's generated weekly placement. */
  public record CourseAssignmentView(
      long courseId,
      String courseCode,
      String courseName,
      int period,
      String dayOfWeek,
      String startsAt) {}
}
