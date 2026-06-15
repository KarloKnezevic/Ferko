package hr.fer.zemris.ferko.application.usecase.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.PasswordHasher;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.support.InMemoryExamRepository;
import hr.fer.zemris.ferko.application.support.InMemoryGradingRepository;
import hr.fer.zemris.ferko.application.usecase.admin.AdminStudentViews.AdminStudentProfileView;
import hr.fer.zemris.ferko.application.usecase.admin.AdminStudentViews.PasswordResetView;
import hr.fer.zemris.ferko.application.usecase.calendar.CalendarService;
import hr.fer.zemris.ferko.application.usecase.profile.AcademicSummaryService;
import hr.fer.zemris.ferko.application.usecase.student.StudentGradesService;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.GroupType;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminStudentServiceTest {

  private static final class FakeSchedule implements ClassScheduleRepository {
    private final List<ClassSchedule> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public ClassSchedule save(ClassSchedule e) {
      ClassSchedule saved =
          new ClassSchedule(
              ++seq,
              e.courseId(),
              e.groupId(),
              e.type(),
              e.roomId(),
              e.dayOfWeek(),
              e.startsAt(),
              e.endsAt(),
              e.instructor());
      store.add(saved);
      return saved;
    }

    @Override
    public List<ClassSchedule> findByCourse(long courseId) {
      return store.stream().filter(s -> s.courseId() == courseId).toList();
    }

    @Override
    public List<ClassSchedule> findAll() {
      return List.copyOf(store);
    }

    @Override
    public Optional<ClassSchedule> findById(long id) {
      return store.stream().filter(s -> s.id() == id).findFirst();
    }

    @Override
    public boolean updatePlacement(
        long id, DayOfWeek dayOfWeek, LocalTime startsAt, LocalTime endsAt, Long roomId) {
      return false;
    }

    @Override
    public int deleteByCourse(long courseId) {
      return 0;
    }
  }

  private InMemoryAcademicRepositories.Users users;
  private InMemoryAcademicRepositories.Students students;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private InMemoryAcademicRepositories.Courses courses;
  private InMemoryAcademicRepositories.Rooms rooms;
  private InMemoryGradingRepository grading;
  private InMemoryExamRepository exams;
  private FakeSchedule schedule;
  private AdminStudentService service;

  @BeforeEach
  void setUp() {
    users = new InMemoryAcademicRepositories.Users();
    students = new InMemoryAcademicRepositories.Students();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    courses = new InMemoryAcademicRepositories.Courses();
    rooms = new InMemoryAcademicRepositories.Rooms();
    grading = new InMemoryGradingRepository();
    exams = new InMemoryExamRepository();
    schedule = new FakeSchedule();
    StudentGradesService grades =
        new StudentGradesService(users, students, enrollments, courses, grading);
    AcademicSummaryService summary =
        new AcademicSummaryService(grades, users, courses, enrollments, schedule);
    CalendarService calendar =
        new CalendarService(users, students, enrollments, courses, exams, schedule, rooms);
    PasswordHasher hasher = raw -> "hashed:" + raw;
    service = new AdminStudentService(users, students, grades, summary, calendar, hasher);
  }

  private long student(String username) {
    long userId =
        users
            .save(
                new AppUser(
                    0L,
                    username,
                    "old-hash",
                    "Test Student",
                    username + "@fer.hr",
                    true,
                    LocalDateTime.now(),
                    Set.of(Role.STUDENT)))
            .id();
    long studentId = students.save(new Student(0L, userId, "0036000001", "Računarstvo", 2)).id();
    long courseId = courses.save(new Course(0L, "PROG", "Programiranje", "2025Z", 6, "", "")).id();
    enrollments.save(
        new Enrollment(0L, studentId, courseId, LocalDateTime.now(), EnrollmentStatus.ACTIVE));
    schedule.save(
        new ClassSchedule(
            0L,
            courseId,
            null,
            GroupType.LECTURE,
            null,
            DayOfWeek.MONDAY,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            "Prof"));
    return userId;
  }

  @Test
  void profileAggregatesIdentityRolesEnrolmentAndSchedule() {
    long userId = student("student.ana");

    AdminStudentProfileView profile = service.profile(userId).orElseThrow();

    assertEquals("student.ana", profile.username());
    assertEquals("Test Student", profile.fullName());
    assertTrue(profile.student());
    assertTrue(profile.roles().contains("STUDENT"));
    assertEquals("0036000001", profile.jmbag());
    assertEquals(1, profile.courses().size()); // one enrolled course (PROG)
    assertEquals(1, profile.weekly().size()); // its Monday lecture
    assertEquals("MONDAY", profile.weekly().get(0).dayOfWeek());
  }

  @Test
  void resetPasswordPersistsOnlyTheHashAndEchoesTheNewPassword() {
    long userId = student("student.ana");

    PasswordResetView reset = service.resetPassword(userId, "Temp-One-Time-42").orElseThrow();

    assertEquals("student.ana", reset.username());
    assertEquals("Temp-One-Time-42", reset.temporaryPassword());
    assertFalse(reset.temporaryPassword().isBlank());
    // Only the hash of the new password is stored — never the old hash, never clear text.
    String storedHash = users.findById(userId).orElseThrow().passwordHash();
    assertEquals("hashed:Temp-One-Time-42", storedHash);
  }

  @Test
  void profileOfUnknownUserIsEmpty() {
    assertTrue(service.profile(999L).isEmpty());
    assertTrue(service.resetPassword(999L, "x").isEmpty());
  }
}
