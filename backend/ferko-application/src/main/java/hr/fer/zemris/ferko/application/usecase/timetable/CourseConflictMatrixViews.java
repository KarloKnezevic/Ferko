package hr.fer.zemris.ferko.application.usecase.timetable;

import java.util.List;

/** View DTOs for the course-overlap conflict matrix (shared students between course pairs). */
public final class CourseConflictMatrixViews {

  private CourseConflictMatrixViews() {}

  /**
   * The conflict matrix for one semester. {@code axis} is the ordered list of courses (row = column
   * = the same index); {@code cells} is the sparse upper triangle of shared-student counts; {@code
   * maxShared} is the largest cell value (for colour scaling). An empty {@code cells} list means no
   * two courses share a student.
   */
  public record CourseConflictMatrixView(
      String semesterCode, List<CourseAxis> axis, List<MatrixCell> cells, int maxShared) {}

  /**
   * One axis entry: the course at a given matrix index, with its own enrolment ({@code enrolled}).
   */
  public record CourseAxis(long courseId, String code, String name, int enrolled) {}

  /**
   * Shared-student count between the courses at indices {@code i} and {@code j} ({@code i < j}).
   */
  public record MatrixCell(int i, int j, int shared) {}
}
