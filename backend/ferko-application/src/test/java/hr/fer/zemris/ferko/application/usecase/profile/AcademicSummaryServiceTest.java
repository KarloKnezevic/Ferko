package hr.fer.zemris.ferko.application.usecase.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.support.InMemoryGradingRepository;
import hr.fer.zemris.ferko.application.usecase.academic.AcademicProvisioningService;
import hr.fer.zemris.ferko.application.usecase.student.StudentGradesService;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Grade;
import hr.fer.zemris.ferko.domain.model.GroupType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AcademicSummaryServiceTest {

  /** Minimal storing class-schedule fake so weekly-hours summation can be exercised. */
  private static final class FakeSchedule implements ClassScheduleRepository {
    private final List<ClassSchedule> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public ClassSchedule save(ClassSchedule entry) {
      ClassSchedule saved =
          new ClassSchedule(
              ++seq,
              entry.courseId(),
              entry.groupId(),
              entry.type(),
              entry.roomId(),
              entry.dayOfWeek(),
              entry.startsAt(),
              entry.endsAt(),
              entry.instructor());
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
    public java.util.Optional<ClassSchedule> findById(long id) {
      return store.stream().filter(s -> s.id() == id).findFirst();
    }

    @Override
    public boolean updatePlacement(
        long id,
        java.time.DayOfWeek dayOfWeek,
        java.time.LocalTime startsAt,
        java.time.LocalTime endsAt,
        Long roomId) {
      for (int i = 0; i < store.size(); i++) {
        ClassSchedule s = store.get(i);
        if (s.id() == id) {
          store.set(
              i,
              new ClassSchedule(
                  s.id(),
                  s.courseId(),
                  s.groupId(),
                  s.type(),
                  roomId,
                  dayOfWeek,
                  startsAt,
                  endsAt,
                  s.instructor()));
          return true;
        }
      }
      return false;
    }

    @Override
    public int deleteByCourse(long courseId) {
      return 0;
    }
  }

  private InMemoryAcademicRepositories.Users users;
  private InMemoryAcademicRepositories.Courses courses;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private InMemoryGradingRepository grading;
  private FakeSchedule schedule;
  private AcademicProvisioningService provisioning;
  private AcademicSummaryService summary;

  @BeforeEach
  void setUp() {
    users = new InMemoryAcademicRepositories.Users();
    InMemoryAcademicRepositories.Semesters semesters = new InMemoryAcademicRepositories.Semesters();
    courses = new InMemoryAcademicRepositories.Courses();
    InMemoryAcademicRepositories.Students students = new InMemoryAcademicRepositories.Students();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    InMemoryAcademicRepositories.Rooms rooms = new InMemoryAcademicRepositories.Rooms();
    grading = new InMemoryGradingRepository();
    schedule = new FakeSchedule();
    provisioning =
        new AcademicProvisioningService(
            semesters, courses, enrollments, students, rooms, users, schedule);
    StudentGradesService grades =
        new StudentGradesService(users, students, enrollments, courses, grading);
    summary = new AcademicSummaryService(grades, users, courses, enrollments, schedule);

    provisioning.provisionSemester(
        "2026LJ",
        "2025/2026",
        "LJETNI",
        LocalDate.parse("2026-03-01"),
        LocalDate.parse("2026-07-15"),
        true);
  }

  @Test
  void studySummaryAggregatesEctsAndGradeAverages() {
    long courseA = provisioning.provisionCourse("PROG", "Programiranje", "2026LJ", 6, "o", "l");
    long courseB = provisioning.provisionCourse("MAT", "Matematika", "2026LJ", 4, "o", "l");
    long studentId =
        provisioning.provisionStudent(
            "0036501030", "h", "Ana A", "ana@fer.hr", "0036501030", "R", 1, LocalDateTime.now());
    provisioning.enroll(studentId, courseA, LocalDateTime.now());
    provisioning.enroll(studentId, courseB, LocalDateTime.now());
    grading.saveGrade(new Grade(0L, courseA, studentId, 5, 90.0, "admin", LocalDateTime.now()));
    grading.saveGrade(new Grade(0L, courseB, studentId, 1, 20.0, "admin", LocalDateTime.now()));

    StudentStudySummaryView view = summary.studySummary("0036501030");
    assertEquals(2, view.enrolledCourses());
    assertEquals(2, view.gradedCourses());
    assertEquals(1, view.passedCourses());
    assertEquals(10, view.ectsEnrolled());
    assertEquals(6, view.ectsEarned());
    assertEquals(3.0, view.averageGrade(), 0.0001);
    // ECTS-weighted: (5*6 + 1*4) / (6+4) = 3.4
    assertEquals(3.4, view.weightedGpa(), 0.0001);
  }

  @Test
  void studySummaryIsEmptyForNonStudent() {
    StudentStudySummaryView view = summary.studySummary("nobody");
    assertEquals(0, view.enrolledCourses());
    assertEquals(0, view.ectsEarned());
  }

  @Test
  void teachingLoadCountsCoursesStudentsAndWeeklyHours() {
    long courseA = provisioning.provisionCourse("PROG", "Programiranje", "2026LJ", 6, "o", "l");
    long courseB = provisioning.provisionCourse("ALG", "Algoritmi", "2026LJ", 5, "o", "l");
    long lecturerId =
        provisioning.provisionStaffUser(
            "lecturer.marko", "h", "Marko P", "m@fer.hr", Set.of("NOSITELJ"), LocalDateTime.now());
    provisioning.assignStaff(courseA, lecturerId, "NOSITELJ");
    provisioning.assignStaff(courseB, lecturerId, "NOSITELJ");

    // Two students on course A, one on course B.
    long s1 =
        provisioning.provisionStudent(
            "0036501031", "h", "A", "a@fer.hr", "0036501031", "R", 1, LocalDateTime.now());
    long s2 =
        provisioning.provisionStudent(
            "0036501032", "h", "B", "b@fer.hr", "0036501032", "R", 1, LocalDateTime.now());
    provisioning.enroll(s1, courseA, LocalDateTime.now());
    provisioning.enroll(s2, courseA, LocalDateTime.now());
    provisioning.enroll(s1, courseB, LocalDateTime.now());

    // Course A: 2h lecture; course B: 1.5h lecture.
    schedule.save(
        new ClassSchedule(
            0L,
            courseA,
            null,
            GroupType.LECTURE,
            null,
            java.time.DayOfWeek.MONDAY,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            "Marko"));
    schedule.save(
        new ClassSchedule(
            0L,
            courseB,
            null,
            GroupType.LECTURE,
            null,
            java.time.DayOfWeek.TUESDAY,
            LocalTime.of(9, 0),
            LocalTime.of(10, 30),
            "Marko"));

    TeachingLoadView view = summary.teachingLoad("lecturer.marko");
    assertEquals(2, view.courseCount());
    assertEquals(3, view.totalStudents());
    assertEquals(3.5, view.weeklyHours(), 0.0001);
    assertTrue(
        view.courses().stream().anyMatch(c -> c.code().equals("PROG") && c.weeklyHours() == 2.0));
  }

  @Test
  void teachingLoadIsEmptyForUserWhoTeachesNothing() {
    provisioning.provisionStaffUser(
        "idle.user", "h", "Idle", "i@fer.hr", Set.of("NASTAVNIK"), LocalDateTime.now());
    TeachingLoadView view = summary.teachingLoad("idle.user");
    assertEquals(0, view.courseCount());
    assertTrue(view.courses().isEmpty());
  }
}
