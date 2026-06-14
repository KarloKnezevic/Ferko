package hr.fer.zemris.ferko.application.usecase.student;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.GradingRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.GradeComponent;
import hr.fer.zemris.ferko.domain.model.Student;
import hr.fer.zemris.ferko.domain.model.StudentPoints;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Builds the "Moji bodovi" view for the signed-in student: per enrolled course, their points for
 * each grade component, the running total against the maximum and the final grade.
 */
public class StudentGradesService {

  private final AppUserRepository userRepository;
  private final StudentRepository studentRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;
  private final GradingRepository gradingRepository;

  public StudentGradesService(
      AppUserRepository userRepository,
      StudentRepository studentRepository,
      EnrollmentRepository enrollmentRepository,
      CourseRepository courseRepository,
      GradingRepository gradingRepository) {
    this.userRepository = userRepository;
    this.studentRepository = studentRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.courseRepository = courseRepository;
    this.gradingRepository = gradingRepository;
  }

  /** Returns an empty list when the user is not a student. */
  public List<MyCourseGradeView> forStudent(String username) {
    Optional<AppUser> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      return List.of();
    }
    Optional<Student> student = studentRepository.findByUserId(user.get().id());
    if (student.isEmpty()) {
      return List.of();
    }
    long studentId = student.get().id();

    List<MyCourseGradeView> result = new ArrayList<>();
    for (Enrollment enrollment : enrollmentRepository.findByStudent(studentId)) {
      Course course = courseRepository.findById(enrollment.courseId()).orElse(null);
      if (course == null) {
        continue;
      }
      result.add(buildCourse(course, studentId));
    }
    return result;
  }

  private MyCourseGradeView buildCourse(Course course, long studentId) {
    long courseId = course.id();
    List<GradeComponent> components = gradingRepository.componentsByCourse(courseId);
    List<StudentPoints> mine =
        gradingRepository.pointsByCourse(courseId).stream()
            .filter(p -> p.studentId() == studentId && p.componentId() != null && p.published())
            .toList();

    List<MyCourseGradeView.ComponentPoints> lines = new ArrayList<>();
    double total = 0.0;
    double maxTotal = 0.0;
    for (GradeComponent component : components) {
      double points =
          mine.stream()
              .filter(p -> p.componentId() == component.id())
              .mapToDouble(StudentPoints::points)
              .sum();
      lines.add(
          new MyCourseGradeView.ComponentPoints(
              component.shortName(), component.name(), points, component.maxPoints()));
      total += points;
      maxTotal += component.maxPoints();
    }

    int finalGrade =
        gradingRepository.gradeFor(courseId, studentId).map(grade -> grade.finalGrade()).orElse(0);

    return new MyCourseGradeView(
        courseId, course.code(), course.name(), lines, total, maxTotal, finalGrade);
  }
}
