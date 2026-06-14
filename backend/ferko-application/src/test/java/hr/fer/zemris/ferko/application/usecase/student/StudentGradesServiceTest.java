package hr.fer.zemris.ferko.application.usecase.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.support.InMemoryGradingRepository;
import hr.fer.zemris.ferko.application.usecase.grading.GradingService;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudentGradesServiceTest {

  private InMemoryAcademicRepositories.Users users;
  private InMemoryAcademicRepositories.Students students;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private InMemoryAcademicRepositories.Courses courses;
  private InMemoryGradingRepository grading;
  private GradingService gradingService;
  private StudentGradesService service;

  private long studentId;
  private long courseId;

  @BeforeEach
  void setUp() {
    users = new InMemoryAcademicRepositories.Users();
    students = new InMemoryAcademicRepositories.Students();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    courses = new InMemoryAcademicRepositories.Courses();
    grading = new InMemoryGradingRepository();
    gradingService = new GradingService(grading, enrollments, students, users);
    service = new StudentGradesService(users, students, enrollments, courses, grading);

    AppUser user =
        users.save(
            new AppUser(
                0L,
                "student.ana",
                "x",
                "Ana Anić",
                "ana@fer.hr",
                true,
                LocalDateTime.now(),
                Set.of(Role.STUDENT)));
    studentId = students.save(new Student(0L, user.id(), "0036000001", "Računarstvo", 2)).id();
    courseId = courses.save(new Course(0L, "PROG", "Programiranje", "2025/26-ZS", 6, "", "")).id();
    enrollments.save(
        new Enrollment(0L, studentId, courseId, LocalDateTime.now(), EnrollmentStatus.ACTIVE));
  }

  @Test
  void aggregatesPointsComponentsAndGrade() {
    var mi1 = gradingService.addComponent(courseId, "Prvi međuispit", "MI1", 20.0, 0);
    var zi = gradingService.addComponent(courseId, "Završni ispit", "ZI", 30.0, 1);
    gradingService.enterPoints(courseId, studentId, mi1.id(), 15.0, "lecturer.marko");
    gradingService.enterPoints(courseId, studentId, zi.id(), 20.0, "lecturer.marko");
    gradingService.assignGrade(courseId, studentId, 4, "lecturer.marko");

    List<MyCourseGradeView> mine = service.forStudent("student.ana");
    assertEquals(1, mine.size());
    MyCourseGradeView view = mine.get(0);
    assertEquals("PROG", view.courseCode());
    assertEquals(2, view.components().size());
    assertEquals(35.0, view.totalPoints());
    assertEquals(50.0, view.maxPoints());
    assertEquals(4, view.finalGrade());
    assertEquals("MI1", view.components().get(0).shortName());
    assertEquals(15.0, view.components().get(0).points());
    assertEquals(20.0, view.components().get(0).maxPoints());
  }

  @Test
  void zeroPointsAndNoGradeWhenNothingEntered() {
    gradingService.addComponent(courseId, "Prvi međuispit", "MI1", 20.0, 0);
    MyCourseGradeView view = service.forStudent("student.ana").get(0);
    assertEquals(0.0, view.totalPoints());
    assertEquals(20.0, view.maxPoints());
    assertEquals(0, view.finalGrade());
  }

  @Test
  void emptyForNonStudentAndUnknownUser() {
    users.save(
        new AppUser(
            0L,
            "admin.x",
            "x",
            "Admin",
            "a@fer.hr",
            true,
            LocalDateTime.now(),
            Set.of(Role.ADMIN)));
    assertTrue(service.forStudent("admin.x").isEmpty());
    assertTrue(service.forStudent("ne.postoji").isEmpty());
  }
}
