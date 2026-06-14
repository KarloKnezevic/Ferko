package hr.fer.zemris.ferko.application.usecase.timetable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingViews.GeneratedTimetableView;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class LectureTimetablingServiceTest {

  private final InMemoryAcademicRepositories.Courses courses =
      new InMemoryAcademicRepositories.Courses();
  private final InMemoryAcademicRepositories.Enrollments enrollments =
      new InMemoryAcademicRepositories.Enrollments();
  private final InMemoryAcademicRepositories.Students students =
      new InMemoryAcademicRepositories.Students();
  private final LectureTimetablingService service =
      new LectureTimetablingService(courses, enrollments, students);

  @Test
  void generatesConflictMinimisingAssignment() {
    long a = courses.save(course("A", "Algebra")).id();
    long b = courses.save(course("B", "Baze")).id();
    long c = courses.save(course("C", "Cyber")).id();
    long s1 = students.save(student("0001", 1)).id();
    long s2 = students.save(student("0002", 1)).id();
    // s1 takes A and B -> A,B conflict. s2 takes C only.
    enroll(s1, a);
    enroll(s1, b);
    enroll(s2, c);

    GeneratedTimetableView result = service.generate(List.of(a, b, c), 5, "GENETIC");

    assertEquals(3, result.courses());
    assertEquals(1, result.baselineConflicts());
    // With 5 periods the engine can place A and B apart -> conflict-free.
    assertEquals(0, result.resultConflicts());
    assertTrue(result.feasible());
    assertFalse(result.convergence().isEmpty());
    assertEquals(3, result.assignments().size());
  }

  @Test
  void resolvesCoursesForStudyYear() {
    long a = courses.save(course("A", "Algebra")).id();
    long b = courses.save(course("B", "Baze")).id();
    long firstYear = students.save(student("0001", 1)).id();
    long thirdYear = students.save(student("0003", 3)).id();
    enroll(firstYear, a);
    enroll(thirdYear, b);

    assertEquals(List.of(a), service.coursesForStudyYear(1));
    assertEquals(List.of(b), service.coursesForStudyYear(3));
  }

  @Test
  void emptyScopeIsHandled() {
    GeneratedTimetableView result = service.generate(List.of(), 5, "");
    assertEquals(0, result.courses());
    assertTrue(result.feasible());
  }

  private static Course course(String code, String name) {
    return new Course(0L, code, name, "2026LJ", 5, "", "");
  }

  private static Student student(String jmbag, int year) {
    return new Student(0L, 0L, jmbag, "Računarstvo", year);
  }

  private void enroll(long studentId, long courseId) {
    enrollments.save(
        new Enrollment(0L, studentId, courseId, LocalDateTime.now(), EnrollmentStatus.ACTIVE));
  }
}
