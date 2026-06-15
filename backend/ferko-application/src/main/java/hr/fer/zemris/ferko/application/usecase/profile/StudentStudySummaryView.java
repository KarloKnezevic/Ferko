package hr.fer.zemris.ferko.application.usecase.profile;

/**
 * Aggregate study record for the signed-in student: how many courses they attend, how many are
 * graded/passed, ECTS enrolled vs. earned, and their (ECTS-weighted) grade averages. All-zero when
 * the user is not a student.
 */
public record StudentStudySummaryView(
    int enrolledCourses,
    int gradedCourses,
    int passedCourses,
    int ectsEnrolled,
    int ectsEarned,
    double averageGrade,
    double weightedGpa) {}
