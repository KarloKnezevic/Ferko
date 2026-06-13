package hr.fer.zemris.ferko.application.usecase.grading;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.support.InMemoryGradingRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.LocalDateTime;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GradingServiceTest {

  private static final long COURSE = 100L;

  private InMemoryGradingRepository grading;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private InMemoryAcademicRepositories.Students students;
  private InMemoryAcademicRepositories.Users users;
  private GradingService service;
  private long studentId;

  @BeforeEach
  void setUp() {
    grading = new InMemoryGradingRepository();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    students = new InMemoryAcademicRepositories.Students();
    users = new InMemoryAcademicRepositories.Users();
    service = new GradingService(grading, enrollments, students, users);

    long userId =
        users
            .save(
                new AppUser(
                    0L,
                    "ana",
                    "h",
                    "Ana Anić",
                    "ana@fer.hr",
                    true,
                    LocalDateTime.now(),
                    EnumSet.of(Role.STUDENT)))
            .id();
    studentId = students.save(new Student(0L, userId, "0036500001", "Računarstvo", 1)).id();
    enrollments.save(
        new Enrollment(0L, studentId, COURSE, LocalDateTime.now(), EnrollmentStatus.ACTIVE));
  }

  @Test
  void componentsPointsOverviewAndGrades() {
    GradeComponentView mi = service.addComponent(COURSE, "Međuispit", "MI", 20.0, 1);
    GradeComponentView zi = service.addComponent(COURSE, "Završni", "ZI", 30.0, 2);
    assertEquals(2, service.listComponents(COURSE).size());

    service.enterPoints(COURSE, studentId, mi.id(), 14.0, "lecturer");
    service.enterPoints(COURSE, studentId, zi.id(), 21.0, "lecturer");
    // Re-entry overwrites.
    service.enterPoints(COURSE, studentId, mi.id(), 16.0, "lecturer");

    PointsOverviewRow row = service.pointsOverview(COURSE).get(0);
    assertEquals("0036500001", row.jmbag());
    assertEquals("Ana Anić", row.fullName());
    assertEquals(16.0, row.pointsByComponent().get("MI"));
    assertEquals(21.0, row.pointsByComponent().get("ZI"));
    assertEquals(37.0, row.total());
    assertEquals(0, row.finalGrade());

    service.assignGrade(COURSE, studentId, 4, "lecturer");
    assertEquals(4, service.pointsOverview(COURSE).get(0).finalGrade());
    assertEquals(4, service.listGrades(COURSE).get(0).finalGrade());
    assertEquals(37.0, service.listGrades(COURSE).get(0).pointsTotal());
  }
}
