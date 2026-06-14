package hr.fer.zemris.ferko.application.usecase.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.support.InMemoryExamRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamKind;
import hr.fer.zemris.ferko.domain.model.ExamRegistration;
import hr.fer.zemris.ferko.domain.model.ExamSeat;
import hr.fer.zemris.ferko.domain.model.ExamVisibility;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Room;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudentExamServiceTest {

  private InMemoryAcademicRepositories.Users users;
  private InMemoryAcademicRepositories.Students students;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private InMemoryAcademicRepositories.Courses courses;
  private InMemoryAcademicRepositories.Rooms rooms;
  private InMemoryExamRepository exams;
  private StudentExamService service;

  private long studentId;
  private long courseId;
  private long examId;
  private long roomId;

  @BeforeEach
  void setUp() {
    users = new InMemoryAcademicRepositories.Users();
    students = new InMemoryAcademicRepositories.Students();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    courses = new InMemoryAcademicRepositories.Courses();
    rooms = new InMemoryAcademicRepositories.Rooms();
    exams = new InMemoryExamRepository();
    service = new StudentExamService(users, students, enrollments, courses, exams, rooms);

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
    examId =
        exams
            .save(
                new Exam(
                    0L,
                    courseId,
                    "Prvi međuispit",
                    "MI1",
                    ExamKind.MEDJUISPIT,
                    LocalDateTime.now().plusDays(7),
                    90,
                    20.0,
                    0,
                    ExamVisibility.ALWAYS,
                    false,
                    null,
                    false))
            .id();
    roomId = rooms.save(new Room(0L, "A101", "Zgrada A", 60, 2, false)).id();
  }

  @Test
  void listsExamsOfEnrolledCoursesWithoutSeatingUntilPublished() {
    List<MyExamView> mine = service.forStudent("student.ana");
    assertEquals(1, mine.size());
    MyExamView view = mine.get(0);
    assertEquals("MI1", view.shortName());
    assertEquals("PROG", view.courseCode());
    assertFalse(view.published());
    assertFalse(view.registered());
    assertNull(view.roomCode());
    assertNull(view.seatNo());
  }

  @Test
  void revealsRoomAndSeatOncePublished() {
    exams.addRegistration(
        new ExamRegistration(0L, examId, studentId, LocalDateTime.now(), "REGISTERED"));
    exams.replaceSeats(examId, List.of(new ExamSeat(0L, examId, studentId, roomId, 12, "A")));
    exams.markPublished(examId, true);

    MyExamView view = service.forStudent("student.ana").get(0);
    assertTrue(view.published());
    assertTrue(view.registered());
    assertEquals("A101", view.roomCode());
    assertEquals(12, view.seatNo());
    assertEquals("A", view.testGroup());
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
