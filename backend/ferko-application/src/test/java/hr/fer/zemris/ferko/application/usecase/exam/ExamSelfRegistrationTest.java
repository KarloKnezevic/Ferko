package hr.fer.zemris.ferko.application.usecase.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.support.InMemoryExamRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamKind;
import hr.fer.zemris.ferko.domain.model.ExamVisibility;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExamSelfRegistrationTest {

  private InMemoryExamRepository exams;
  private InMemoryAcademicRepositories.Students students;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private InMemoryAcademicRepositories.Users users;
  private ExamSchedulingService service;

  private long studentId;
  private final long courseId = 3L;
  private long examId;

  @BeforeEach
  void setUp() {
    exams = new InMemoryExamRepository();
    students = new InMemoryAcademicRepositories.Students();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    users = new InMemoryAcademicRepositories.Users();
    service =
        new ExamSchedulingService(
            exams,
            new InMemoryAcademicRepositories.Rooms(),
            students,
            enrollments,
            users,
            (recipients, subject, body) -> {});

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
    enrollments.save(
        new Enrollment(0L, studentId, courseId, LocalDateTime.now(), EnrollmentStatus.ACTIVE));
    examId = exams.save(unpublishedExam()).id();
  }

  private Exam unpublishedExam() {
    return new Exam(
        0L,
        courseId,
        "Međuispit",
        "MI1",
        ExamKind.MEDJUISPIT,
        LocalDateTime.now().plusDays(3),
        90,
        20.0,
        0,
        ExamVisibility.ALWAYS,
        false,
        null,
        false);
  }

  @Test
  void registersAndUnregistersIdempotently() {
    assertTrue(service.registerSelf(examId, "student.ana"));
    assertEquals(1, exams.findRegistrations(examId).size());
    assertTrue(service.registerSelf(examId, "student.ana"));
    assertEquals(1, exams.findRegistrations(examId).size(), "idempotent");

    assertTrue(service.unregisterSelf(examId, "student.ana"));
    assertEquals(0, exams.findRegistrations(examId).size());
  }

  @Test
  void rejectsUnknownExamNonStudentAndNotEnrolled() {
    assertFalse(service.registerSelf(999L, "student.ana"));
    assertFalse(service.registerSelf(examId, "ne.postoji"));

    AppUser other =
        users.save(
            new AppUser(
                0L,
                "student.ivo",
                "x",
                "Ivo",
                "ivo@fer.hr",
                true,
                LocalDateTime.now(),
                Set.of(Role.STUDENT)));
    students.save(new Student(0L, other.id(), "0036000002", "Računarstvo", 2));
    assertFalse(service.registerSelf(examId, "student.ivo"), "not enrolled in the course");
  }

  @Test
  void registrationClosedOncePublished() {
    long published = exams.save(publishedExam()).id();
    assertThrows(IllegalStateException.class, () -> service.registerSelf(published, "student.ana"));
    assertThrows(
        IllegalStateException.class, () -> service.unregisterSelf(published, "student.ana"));
  }

  private Exam publishedExam() {
    return new Exam(
        0L,
        courseId,
        "Završni",
        "ZI",
        ExamKind.ZAVRSNI,
        LocalDateTime.now().plusDays(10),
        120,
        30.0,
        1,
        ExamVisibility.ALWAYS,
        false,
        null,
        true);
  }
}
