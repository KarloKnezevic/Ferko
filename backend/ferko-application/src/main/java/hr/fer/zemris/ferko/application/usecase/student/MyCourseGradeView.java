package hr.fer.zemris.ferko.application.usecase.student;

import java.util.List;

/**
 * The signed-in student's points and final grade for one enrolled course: the per-component points
 * they have earned, the running total against the maximum, and the final grade (0 = not yet
 * graded).
 */
public record MyCourseGradeView(
    long courseId,
    String courseCode,
    String courseName,
    List<ComponentPoints> components,
    double totalPoints,
    double maxPoints,
    int finalGrade) {

  /** One grade component with the points the student earned out of its maximum. */
  public record ComponentPoints(String shortName, String name, double points, double maxPoints) {}
}
