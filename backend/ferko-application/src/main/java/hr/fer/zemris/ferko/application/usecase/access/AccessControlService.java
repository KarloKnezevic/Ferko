package hr.fer.zemris.ferko.application.usecase.access;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Student;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * Row-level authorization for course-scoped data. Answers the question "may this user see the
 * contents of this course?" so controllers can enforce that students only reach material for the
 * courses they attend, teaching staff only for the courses they teach, while {@code ADMIN} and
 * {@code STUSLU} retain faculty-wide visibility.
 */
public class AccessControlService {

  /** Roles that grant faculty-wide visibility regardless of enrollment or teaching assignment. */
  private static final Set<String> GLOBAL_ROLES = Set.of("ADMIN", "STUSLU");

  private final AppUserRepository users;
  private final StudentRepository students;
  private final EnrollmentRepository enrollments;
  private final CourseRepository courses;

  public AccessControlService(
      AppUserRepository users,
      StudentRepository students,
      EnrollmentRepository enrollments,
      CourseRepository courses) {
    this.users = users;
    this.students = students;
    this.enrollments = enrollments;
    this.courses = courses;
  }

  /**
   * Returns {@code true} when the user may view the given course's content. Access is granted to
   * holders of a global role, to staff teaching the course, and to students enrolled in it.
   *
   * @param username the authenticated principal name
   * @param roles the user's role names without the {@code ROLE_} prefix
   * @param courseId the course being accessed
   */
  public boolean canAccessCourse(String username, Collection<String> roles, long courseId) {
    if (roles != null && roles.stream().anyMatch(GLOBAL_ROLES::contains)) {
      return true;
    }
    Optional<AppUser> user = users.findByUsername(username);
    if (user.isEmpty()) {
      return false;
    }
    long userId = user.get().id();
    return teaches(userId, courseId) || isEnrolled(userId, courseId);
  }

  /**
   * Returns {@code true} when the user may manage (not merely view) the given course's content —
   * holders of a global role, or staff teaching the course. Unlike {@link #canAccessCourse}, an
   * enrolled student does <em>not</em> pass, so this gates write actions such as deleting notices.
   *
   * @param username the authenticated principal name
   * @param roles the user's role names without the {@code ROLE_} prefix
   * @param courseId the course being managed
   */
  public boolean canManageCourse(String username, Collection<String> roles, long courseId) {
    if (roles != null && roles.stream().anyMatch(GLOBAL_ROLES::contains)) {
      return true;
    }
    Optional<AppUser> user = users.findByUsername(username);
    return user.isPresent() && teaches(user.get().id(), courseId);
  }

  private boolean teaches(long userId, long courseId) {
    return courses.findStaffByCourse(courseId).stream().anyMatch(staff -> staff.userId() == userId);
  }

  private boolean isEnrolled(long userId, long courseId) {
    Optional<Student> student = students.findByUserId(userId);
    if (student.isEmpty()) {
      return false;
    }
    return enrollments.findByStudent(student.get().id()).stream()
        .anyMatch(enrollment -> enrollment.courseId() == courseId);
  }
}
