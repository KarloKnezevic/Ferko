package hr.fer.zemris.ferko.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AcademicModelTest {

  @Test
  void appUserExposesImmutableRolesAndRoleChecks() {
    AppUser user =
        new AppUser(
            1L,
            "admin.ferko",
            "hash",
            "Admin Ferko",
            "admin@fer.hr",
            true,
            LocalDateTime.parse("2026-01-01T08:00:00"),
            EnumSet.of(Role.ADMIN, Role.NOSITELJ));

    assertTrue(user.hasRole(Role.ADMIN));
    assertFalse(user.hasRole(Role.STUDENT));
    assertEquals(2, user.roles().size());
    assertThrows(UnsupportedOperationException.class, () -> user.roles().add(Role.STUDENT));
  }

  @Test
  void appUserToleratesNullRoles() {
    AppUser user = new AppUser(2L, "u", "h", "U", null, true, LocalDateTime.now(), null);
    assertTrue(user.roles().isEmpty());
  }

  @Test
  void academicAggregatesCarryTheirData() {
    Semester semester =
        new Semester(
            "2024Z",
            "2024/2025",
            "ZIMSKI",
            LocalDate.parse("2024-10-01"),
            LocalDate.parse("2025-02-15"),
            true);
    assertEquals("2024Z", semester.code());
    assertTrue(semester.active());

    Course course = new Course(10L, "UURA", "Uvod u računarstvo", "2024Z", 6, "desc", "lit");
    assertEquals("UURA", course.code());
    assertEquals(6, course.ects());

    Student student = new Student(5L, 2L, "0036000000", "Računarstvo", 1);
    assertEquals("0036000000", student.jmbag());

    CourseStaff staff = new CourseStaff(1L, 10L, 2L, Role.NOSITELJ);
    assertEquals(Role.NOSITELJ, staff.role());

    Room room = new Room(3L, "A101", "Zgrada A", 120, 3, false);
    assertEquals(120, room.capacity());

    StudentGroup group = new StudentGroup(7L, 10L, "L01", GroupType.LAB, "Ponedjeljak", 16);
    assertEquals(GroupType.LAB, group.type());

    Enrollment enrollment =
        new Enrollment(9L, 5L, 10L, LocalDateTime.now(), EnrollmentStatus.ACTIVE);
    assertEquals(EnrollmentStatus.ACTIVE, enrollment.status());

    GroupMembership membership = new GroupMembership(4L, 9L, 7L);
    assertEquals(7L, membership.groupId());

    ClassSchedule schedule =
        new ClassSchedule(
            1L,
            10L,
            7L,
            GroupType.LECTURE,
            3L,
            DayOfWeek.MONDAY,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            "prof. X");
    assertEquals(DayOfWeek.MONDAY, schedule.dayOfWeek());
  }

  @Test
  void assessmentAggregatesCarryTheirData() {
    ExamFlag flag = new ExamFlag(1L, 10L, "Položen MI", "MI_OK", "opis");
    assertEquals("MI_OK", flag.shortName());

    Exam exam =
        new Exam(
            2L,
            10L,
            "Prvi međuispit",
            "MI1",
            ExamKind.MEDJUISPIT,
            LocalDateTime.parse("2024-11-15T12:00:00"),
            120,
            20.0,
            1,
            ExamVisibility.ALWAYS,
            false,
            null,
            false);
    assertEquals(ExamKind.MEDJUISPIT, exam.kind());
    assertEquals(120, exam.durationMinutes());

    ExamRegistration registration =
        new ExamRegistration(3L, 2L, 5L, LocalDateTime.now(), "REGISTERED");
    assertEquals(5L, registration.studentId());

    ExamRoom examRoom = new ExamRoom(4L, 2L, 3L, 100, 3, true);
    assertTrue(examRoom.reserved());

    ExamSeat seat = new ExamSeat(6L, 2L, 5L, 3L, 12, "A");
    assertEquals("A", seat.testGroup());

    GradeComponent component = new GradeComponent(8L, 10L, "Međuispit", "MI", 20.0, 1);
    assertEquals(20.0, component.maxPoints());

    StudentPoints points =
        new StudentPoints(
            9L, 10L, 5L, 8L, 2L, 18.5, 20.0, true, "lecturer.marko", LocalDateTime.now());
    assertEquals(18.5, points.points());

    Grade grade = new Grade(11L, 10L, 5L, 4, 78.0, "lecturer.marko", LocalDateTime.now());
    assertEquals(4, grade.finalGrade());

    GroupExchangeRequest request =
        new GroupExchangeRequest(
            12L,
            10L,
            5L,
            7L,
            8L,
            ExchangeStatus.PENDING,
            "Preklapanje",
            null,
            LocalDateTime.now(),
            null);
    assertEquals(ExchangeStatus.PENDING, request.status());
  }

  @Test
  void enumsExposeExpectedConstants() {
    assertEquals(7, Role.values().length);
    assertEquals(Set.of(GroupType.LECTURE, GroupType.LAB), EnumSet.allOf(GroupType.class));
    assertEquals(2, EnrollmentStatus.values().length);
    assertEquals(6, ExamKind.values().length);
    assertEquals(2, ExamVisibility.values().length);
    assertEquals(3, ExchangeStatus.values().length);
  }
}
