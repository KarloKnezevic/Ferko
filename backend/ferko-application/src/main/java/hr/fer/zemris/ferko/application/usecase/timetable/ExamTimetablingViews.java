package hr.fer.zemris.ferko.application.usecase.timetable;

import java.util.List;

/** View DTOs for engine-generated exam timetables and their comparison with the legacy schedule. */
public final class ExamTimetablingViews {

  private ExamTimetablingViews() {}

  /**
   * Result of one exam-timetable generation. Conflicts are weighted by the number of shared
   * students. {@code baselineConflicts} is the all-in-one-slot worst case, {@code resultConflicts}
   * the generated schedule, and {@code legacyConflicts} the historical FER schedule on the same
   * cohort ({@code -1} when no reference was supplied) — letting the engine be judged against the
   * real timetable.
   */
  public record GeneratedExamTimetableView(
      String algorithm,
      int slots,
      int exams,
      long baselineConflicts,
      long resultConflicts,
      long legacyConflicts,
      boolean feasible,
      int iterations,
      List<Double> convergence,
      List<ExamSlotAssignmentView> assignments) {}

  /** A course exam's generated slot and date. */
  public record ExamSlotAssignmentView(
      long courseId, String courseCode, String courseName, int slot, String date) {}
}
