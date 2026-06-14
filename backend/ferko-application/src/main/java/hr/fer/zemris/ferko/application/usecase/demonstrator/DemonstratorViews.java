package hr.fer.zemris.ferko.application.usecase.demonstrator;

/** View DTOs for course demonstrators. */
public final class DemonstratorViews {

  private DemonstratorViews() {}

  /** A demonstrator assigned to a course. */
  public record DemonstratorView(long studentId, String jmbag, String fullName) {}

  /** A course on which the signed-in student is a demonstrator. */
  public record MyDemonstratorDutyView(long courseId, String courseCode, String courseName) {}
}
