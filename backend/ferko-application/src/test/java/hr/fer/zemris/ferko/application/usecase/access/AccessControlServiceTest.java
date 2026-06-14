package hr.fer.zemris.ferko.application.usecase.access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.CourseStaff;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccessControlServiceTest {

  private final InMemoryAcademicRepositories.Users users = new InMemoryAcademicRepositories.Users();
  private final InMemoryAcademicRepositories.Students students =
      new InMemoryAcademicRepositories.Students();
  private final InMemoryAcademicRepositories.Enrollments enrollments =
      new InMemoryAcademicRepositories.Enrollments();
  private final InMemoryAcademicRepositories.Courses courses =
      new InMemoryAcademicRepositories.Courses();

  private final AccessControlService access =
      new AccessControlService(users, students, enrollments, courses);

  private long mathId;
  private long physicsId;

  @BeforeEach
  void seed() {
    Course math = courses.save(course("MATH", "Mathematics"));
    Course physics = courses.save(course("PHYS", "Physics"));
    mathId = math.id();
    physicsId = physics.id();

    // A student enrolled in Mathematics only.
    AppUser studentUser = users.save(user("student.ana", Role.STUDENT));
    Student student = students.save(new Student(0L, studentUser.id(), "0036000001", "FER-2", 2));
    enrollments.save(
        new Enrollment(0L, student.id(), mathId, LocalDateTime.now(), EnrollmentStatus.ACTIVE));

    // A lecturer teaching Physics only.
    AppUser lecturer = users.save(user("lecturer.marko", Role.NOSITELJ));
    courses.addStaff(new CourseStaff(0L, physicsId, lecturer.id(), Role.NOSITELJ));
  }

  @Test
  void adminAndStusluHaveFacultyWideAccess() {
    assertTrue(access.canAccessCourse("anyone", Set.of("ADMIN"), mathId));
    assertTrue(access.canAccessCourse("anyone", Set.of("STUSLU"), physicsId));
  }

  @Test
  void studentReachesOnlyEnrolledCourses() {
    assertTrue(access.canAccessCourse("student.ana", Set.of("STUDENT"), mathId));
    assertFalse(access.canAccessCourse("student.ana", Set.of("STUDENT"), physicsId));
  }

  @Test
  void staffReachOnlyCoursesTheyTeach() {
    assertTrue(access.canAccessCourse("lecturer.marko", Set.of("NOSITELJ"), physicsId));
    assertFalse(access.canAccessCourse("lecturer.marko", Set.of("NOSITELJ"), mathId));
  }

  @Test
  void unknownUserIsDenied() {
    assertFalse(access.canAccessCourse("ghost", Set.of("STUDENT"), mathId));
  }

  @Test
  void nullRolesAreTreatedAsNoGlobalRole() {
    assertTrue(access.canAccessCourse("student.ana", null, mathId));
    assertFalse(access.canAccessCourse("student.ana", null, physicsId));
  }

  private static Course course(String code, String name) {
    return new Course(0L, code, name, "2026LJ", 5, "", "");
  }

  private static AppUser user(String username, Role role) {
    return new AppUser(
        0L,
        username,
        "hash",
        username,
        username + "@fer.hr",
        true,
        LocalDateTime.now(),
        Set.of(role));
  }
}
