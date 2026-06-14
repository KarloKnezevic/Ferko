package hr.fer.zemris.ferko.application.usecase.timetable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.usecase.timetable.ExamTimetablingViews.GeneratedExamTimetableView;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExamTimetablingServiceTest {

  private final InMemoryAcademicRepositories.Courses courses =
      new InMemoryAcademicRepositories.Courses();
  private final InMemoryAcademicRepositories.Enrollments enrollments =
      new InMemoryAcademicRepositories.Enrollments();
  private final InMemoryAcademicRepositories.Students students =
      new InMemoryAcademicRepositories.Students();
  private final ExamTimetablingService service =
      new ExamTimetablingService(courses, enrollments, students);

  @Test
  void generatesConflictFreeExamScheduleAndComparesWithLegacy() {
    long a = courses.save(course("A")).id();
    long b = courses.save(course("B")).id();
    long s1 = students.save(student("0001")).id();
    long s2 = students.save(student("0002")).id();
    // Both students take A and B -> A,B share 2 students.
    enroll(s1, a);
    enroll(s1, b);
    enroll(s2, a);
    enroll(s2, b);

    // Legacy schedule put A and B in the same slot -> 2 weighted conflicts.
    Map<Long, Integer> legacy = Map.of(a, 0, b, 0);
    GeneratedExamTimetableView result = service.generate(List.of(a, b), 5, "GENETIC", legacy);

    assertEquals(2, result.exams());
    assertEquals(2, result.baselineConflicts());
    assertEquals(2, result.legacyConflicts());
    // Engine separates A and B -> conflict-free.
    assertEquals(0, result.resultConflicts());
    assertTrue(result.feasible());
  }

  @Test
  void noLegacyReferenceReportsMinusOne() {
    long a = courses.save(course("A")).id();
    GeneratedExamTimetableView result = service.generate(List.of(a), 3, "GENETIC", Map.of());
    assertEquals(-1, result.legacyConflicts());
  }

  @Test
  void emptyScopeAndUnknownAlgorithmAreHandled() {
    GeneratedExamTimetableView empty = service.generate(List.of(), 5, "NOPE", Map.of());
    assertEquals(0, empty.exams());
    assertTrue(empty.feasible());
    // Unknown algorithm falls back to GENETIC.
    assertEquals("GENETIC", empty.algorithm());

    // Slots are clamped to a sane maximum.
    long a = courses.save(course("A")).id();
    GeneratedExamTimetableView clamped = service.generate(List.of(a), 9999, "GENETIC", Map.of());
    assertTrue(clamped.slots() <= 40);
  }

  private static Course course(String code) {
    return new Course(0L, code, code, "2026LJ", 5, "", "");
  }

  private static Student student(String jmbag) {
    return new Student(0L, 0L, jmbag, "R", 1);
  }

  private void enroll(long studentId, long courseId) {
    enrollments.save(
        new Enrollment(0L, studentId, courseId, LocalDateTime.now(), EnrollmentStatus.ACTIVE));
  }
}
