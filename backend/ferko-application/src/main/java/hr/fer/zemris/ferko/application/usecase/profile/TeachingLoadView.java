package hr.fer.zemris.ferko.application.usecase.profile;

import java.util.List;

/**
 * Teaching workload for the signed-in staff member: the courses they teach with the
 * enrolled-student count and weekly contact hours, plus faculty-wide totals. Empty when the user
 * teaches nothing.
 */
public record TeachingLoadView(
    int courseCount, int totalStudents, double weeklyHours, List<TeachingCourseView> courses) {

  /** One taught course with the staff member's role(s), enrolment and weekly hours on it. */
  public record TeachingCourseView(
      long courseId,
      String code,
      String name,
      String roles,
      int enrolledStudents,
      double weeklyHours) {}
}
