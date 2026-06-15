package hr.fer.zemris.ferko.application.usecase.admin;

import hr.fer.zemris.ferko.application.usecase.calendar.CalendarView.WeeklySlot;
import hr.fer.zemris.ferko.application.usecase.profile.StudentStudySummaryView;
import hr.fer.zemris.ferko.application.usecase.student.MyCourseGradeView;
import java.util.List;

/** View DTOs for the administrative student/user profile (read by ADMIN, never the user's own). */
public final class AdminStudentViews {

  private AdminStudentViews() {}

  /**
   * A full administrative view of one user. Identity fields are always present; {@code student} is
   * {@code true} only when the user has a student record (then {@code jmbag}, {@code studyProgram},
   * {@code yearOfStudy}, {@code summary}, {@code courses} are meaningful). {@code weekly} is the
   * user's recurring timetable (their group's sessions as a student, taught sessions as staff).
   */
  public record AdminStudentProfileView(
      long userId,
      String username,
      String fullName,
      String email,
      boolean active,
      List<String> roles,
      boolean student,
      String jmbag,
      String studyProgram,
      int yearOfStudy,
      StudentStudySummaryView summary,
      List<MyCourseGradeView> courses,
      List<WeeklySlot> weekly) {}

  /**
   * Result of an admin password reset: the freshly generated one-time password to relay to the
   * user. It is shown to the admin once and is not stored in clear text anywhere.
   */
  public record PasswordResetView(String username, String temporaryPassword) {}
}
