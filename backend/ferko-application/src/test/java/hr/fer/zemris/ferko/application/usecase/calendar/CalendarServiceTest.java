package hr.fer.zemris.ferko.application.usecase.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.support.InMemoryExamRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.CourseStaff;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamKind;
import hr.fer.zemris.ferko.domain.model.ExamVisibility;
import hr.fer.zemris.ferko.domain.model.GroupType;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalendarServiceTest {

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
  }

  private InMemoryAcademicRepositories.Users users;
  private InMemoryAcademicRepositories.Courses courses;
  private InMemoryAcademicRepositories.Students students;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private InMemoryAcademicRepositories.Rooms rooms;
  private InMemoryExamRepository exams;
  private FakeSchedule schedule;
  private CalendarService service;

  private long courseId;

  @BeforeEach
  void setUp() {
    users = new InMemoryAcademicRepositories.Users();
    courses = new InMemoryAcademicRepositories.Courses();
    students = new InMemoryAcademicRepositories.Students();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    rooms = new InMemoryAcademicRepositories.Rooms();
    exams = new InMemoryExamRepository();
    schedule = new FakeSchedule();
    service = new CalendarService(users, students, enrollments, courses, exams, schedule, rooms);

    courseId = courses.save(new Course(0L, "FER101", "Fizika 2", "2025Z", 5, null, null)).id();
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
            "Marko Predavač"));
    exams.save(
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
            false));
  }

  @Test
  void studentSeesEnrolledCourseScheduleAndExams() {
    long userId =
        users
            .save(
                new AppUser(
                    0L,
                    "student.test",
                    "h",
                    "Test Student",
                    "t@fer.hr",
                    true,
                    LocalDateTime.now(),
                    EnumSet.of(Role.STUDENT)))
            .id();
    long studentId = students.save(new Student(0L, userId, "0036500001", "Računarstvo", 1)).id();
    enrollments.save(
        new Enrollment(0L, studentId, courseId, LocalDateTime.now(), EnrollmentStatus.ACTIVE));

    CalendarView view = service.forUser("student.test");

    assertEquals(1, view.weekly().size());
    assertEquals("MONDAY", view.weekly().get(0).dayOfWeek());
    assertEquals("08:00", view.weekly().get(0).startsAt());
    assertEquals(1, view.exams().size());
    assertEquals("MI1", view.exams().get(0).shortName());
  }

  @Test
  void staffSeesTaughtCourseSchedule() {
    long userId =
        users
            .save(
                new AppUser(
                    0L,
                    "lecturer.test",
                    "h",
                    "Test Lecturer",
                    "l@fer.hr",
                    true,
                    LocalDateTime.now(),
                    EnumSet.of(Role.NOSITELJ)))
            .id();
    courses.addStaff(new CourseStaff(0L, courseId, userId, Role.NOSITELJ));

    CalendarView view = service.forUser("lecturer.test");

    assertEquals(1, view.weekly().size());
    assertTrue(view.exams().size() >= 1);
  }

  @Test
  void unknownUserGetsEmptyCalendar() {
    CalendarView view = service.forUser("nepostojeci");
    assertTrue(view.weekly().isEmpty());
    assertTrue(view.exams().isEmpty());
  }
}
