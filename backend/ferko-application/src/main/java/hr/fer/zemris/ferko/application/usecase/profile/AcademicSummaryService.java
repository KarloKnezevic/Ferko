package hr.fer.zemris.ferko.application.usecase.profile;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.usecase.student.MyCourseGradeView;
import hr.fer.zemris.ferko.application.usecase.student.StudentGradesService;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.CourseStaff;
import hr.fer.zemris.ferko.domain.model.Role;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Builds the richer profile summaries: a student's study record (ECTS, pass rate, grade averages)
 * and a staff member's teaching load (courses, students, weekly contact hours). Reuses existing
 * read services and ports so no new persistence is introduced.
 */
public class AcademicSummaryService {

  /** Lowest passing grade on the Croatian 1–5 scale (1 is a fail). */
  private static final int PASS_THRESHOLD = 2;

  private final StudentGradesService studentGradesService;
  private final AppUserRepository userRepository;
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final ClassScheduleRepository scheduleRepository;

  public AcademicSummaryService(
      StudentGradesService studentGradesService,
      AppUserRepository userRepository,
      CourseRepository courseRepository,
      EnrollmentRepository enrollmentRepository,
      ClassScheduleRepository scheduleRepository) {
    this.studentGradesService = studentGradesService;
    this.userRepository = userRepository;
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.scheduleRepository = scheduleRepository;
  }

  /** Study summary for the signed-in student; all-zero when the user is not a student. */
  public StudentStudySummaryView studySummary(String username) {
    List<MyCourseGradeView> grades = studentGradesService.forStudent(username);
    int gradedCourses = 0;
    int passedCourses = 0;
    int ectsEnrolled = 0;
    int ectsEarned = 0;
    double gradeSum = 0;
    long weightedGradeSum = 0;
    int weightingEcts = 0;
    for (MyCourseGradeView grade : grades) {
      int ects = courseRepository.findById(grade.courseId()).map(Course::ects).orElse(0);
      ectsEnrolled += ects;
      if (grade.finalGrade() > 0) {
        gradedCourses++;
        gradeSum += grade.finalGrade();
        weightedGradeSum += (long) grade.finalGrade() * ects;
        weightingEcts += ects;
        if (grade.finalGrade() >= PASS_THRESHOLD) {
          passedCourses++;
          ectsEarned += ects;
        }
      }
    }
    double averageGrade = gradedCourses == 0 ? 0 : gradeSum / gradedCourses;
    double weightedGpa = weightingEcts == 0 ? 0 : (double) weightedGradeSum / weightingEcts;
    return new StudentStudySummaryView(
        grades.size(),
        gradedCourses,
        passedCourses,
        ectsEnrolled,
        ectsEarned,
        averageGrade,
        weightedGpa);
  }

  /** Teaching load for the signed-in staff member; empty when they teach nothing. */
  public TeachingLoadView teachingLoad(String username) {
    Optional<AppUser> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      return new TeachingLoadView(0, 0, 0, List.of());
    }
    long userId = user.get().id();
    List<TeachingLoadView.TeachingCourseView> courses = new ArrayList<>();
    int totalStudents = 0;
    double totalWeeklyHours = 0;
    for (Course course : courseRepository.findAll()) {
      List<Role> rolesOnCourse =
          courseRepository.findStaffByCourse(course.id()).stream()
              .filter(member -> member.userId() == userId)
              .map(CourseStaff::role)
              .distinct()
              .toList();
      if (rolesOnCourse.isEmpty()) {
        continue;
      }
      int enrolled = enrollmentRepository.findByCourse(course.id()).size();
      double weeklyHours = weeklyHours(course.id());
      String roles = rolesOnCourse.stream().map(Role::name).collect(Collectors.joining(", "));
      courses.add(
          new TeachingLoadView.TeachingCourseView(
              course.id(), course.code(), course.name(), roles, enrolled, weeklyHours));
      totalStudents += enrolled;
      totalWeeklyHours += weeklyHours;
    }
    return new TeachingLoadView(courses.size(), totalStudents, totalWeeklyHours, courses);
  }

  private double weeklyHours(long courseId) {
    double minutes = 0;
    for (ClassSchedule slot : scheduleRepository.findByCourse(courseId)) {
      if (slot.startsAt() != null && slot.endsAt() != null) {
        minutes += Duration.between(slot.startsAt(), slot.endsAt()).toMinutes();
      }
    }
    return minutes / 60.0;
  }
}
