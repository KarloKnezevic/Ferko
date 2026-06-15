package hr.fer.zemris.ferko.application.usecase.timetable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.usecase.timetable.CourseConflictMatrixViews.CourseConflictMatrixView;
import hr.fer.zemris.ferko.application.usecase.timetable.CourseConflictMatrixViews.MatrixCell;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.Semester;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourseConflictMatrixServiceTest {

  private InMemoryAcademicRepositories.Semesters semesters;
  private InMemoryAcademicRepositories.Courses courses;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private CourseConflictMatrixService service;

  @BeforeEach
  void setUp() {
    semesters = new InMemoryAcademicRepositories.Semesters();
    courses = new InMemoryAcademicRepositories.Courses();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    service = new CourseConflictMatrixService(semesters, courses, enrollments);
    semesters.save(
        new Semester("2026LJ", "2025/2026", "SUMMER", LocalDate.now(), LocalDate.now(), true));
  }

  private long course(String code) {
    return courses.save(new Course(0L, code, code, "2026LJ", 5, "", "")).id();
  }

  private void enroll(long studentId, long courseId) {
    enrollments.save(
        new Enrollment(0L, studentId, courseId, LocalDateTime.now(), EnrollmentStatus.ACTIVE));
  }

  @Test
  void countsSharedStudentsBetweenCoursePairsForTheActiveSemester() {
    long a = course("AAA"); // index 0 (axis is sorted by code)
    long b = course("BBB"); // index 1
    long c = course("CCC"); // index 2
    // Students 1 and 2 take A and B; student 2 also takes C. So A∩B = 2, B∩C = 1, A∩C = 1.
    enroll(1, a);
    enroll(1, b);
    enroll(2, a);
    enroll(2, b);
    enroll(2, c);

    CourseConflictMatrixView m = service.matrix(null);

    assertEquals("2026LJ", m.semesterCode());
    assertEquals(3, m.axis().size());
    assertEquals(2, m.axis().get(0).enrolled()); // AAA has 2 students
    assertEquals(2, shared(m, 0, 1)); // AAA ∩ BBB
    assertEquals(1, shared(m, 1, 2)); // BBB ∩ CCC
    assertEquals(1, shared(m, 0, 2)); // AAA ∩ CCC
    assertEquals(2, m.maxShared());
    // Only the upper triangle is emitted (i < j).
    assertTrue(m.cells().stream().allMatch(cell -> cell.i() < cell.j()));
  }

  @Test
  void coursesWithoutSharedStudentsProduceNoCell() {
    long a = course("AAA");
    long b = course("BBB");
    enroll(1, a);
    enroll(2, b);

    CourseConflictMatrixView m = service.matrix("2026LJ");

    assertTrue(m.cells().isEmpty(), "disjoint courses share no students");
    assertEquals(0, m.maxShared());
  }

  private static int shared(CourseConflictMatrixView m, int i, int j) {
    return m.cells().stream()
        .filter(cell -> cell.i() == i && cell.j() == j)
        .mapToInt(MatrixCell::shared)
        .findFirst()
        .orElse(0);
  }
}
