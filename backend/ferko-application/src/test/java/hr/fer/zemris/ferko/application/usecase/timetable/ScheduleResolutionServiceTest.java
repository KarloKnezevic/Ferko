package hr.fer.zemris.ferko.application.usecase.timetable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.usecase.timetable.ScheduleResolutionViews.ResolutionReportView;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.GroupType;
import hr.fer.zemris.ferko.domain.model.Room;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduleResolutionServiceTest {

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
      store.removeIf(s -> s.courseId() == courseId);
      return 0;
    }
  }

  private InMemoryAcademicRepositories.Courses courses;
  private InMemoryAcademicRepositories.Rooms rooms;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private FakeSchedule schedule;
  private ScheduleResolutionService service;
  private long courseId;

  @BeforeEach
  void setUp() {
    courses = new InMemoryAcademicRepositories.Courses();
    rooms = new InMemoryAcademicRepositories.Rooms();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    schedule = new FakeSchedule();
    service = new ScheduleResolutionService(schedule, courses, rooms, enrollments);

    courseId = courses.save(new Course(0L, "PROG", "Programiranje", "2026LJ", 5, "", "")).id();
    rooms.save(new Room(0L, "R1", "R", 100, 1, false));
    rooms.save(new Room(0L, "R2", "R", 100, 1, false));
  }

  private ClassSchedule slot(Long roomId, DayOfWeek day, int from, int to, String instructor) {
    return schedule.save(
        new ClassSchedule(
            0L,
            courseId,
            null,
            GroupType.LECTURE,
            roomId,
            day,
            LocalTime.of(from, 0),
            LocalTime.of(to, 0),
            instructor));
  }

  @Test
  void detectsRoomAndGroupCollisionsAndAutoResolvesToZero() {
    long room1 = rooms.save(new Room(0L, "RX", "R", 100, 1, false)).id();
    // Two sessions of the same course in the same room at the same time: room + group collision.
    slot(room1, DayOfWeek.MONDAY, 8, 10, "Prof A");
    slot(room1, DayOfWeek.MONDAY, 8, 10, "Prof B");

    ResolutionReportView before = service.report();
    assertEquals(1, before.roomCollisions());
    assertEquals(1, before.groupCollisions());
    assertFalse(before.conflictFree());
    assertTrue(before.collisions().stream().anyMatch(c -> c.suggestion().feasible()));

    ResolutionReportView after = service.autoResolve();
    assertTrue(after.conflictFree(), "auto-resolve should reach zero collisions");
    assertEquals(0, after.roomCollisions());
    assertEquals(0, after.groupCollisions());
  }

  @Test
  void flagsOverCapacityAsAViolation() {
    long small = rooms.save(new Room(0L, "S1", "S", 1, 1, false)).id();
    for (int i = 0; i < 3; i++) {
      enrollments.save(
          new Enrollment(0L, 500 + i, courseId, LocalDateTime.now(), EnrollmentStatus.ACTIVE));
    }
    slot(small, DayOfWeek.WEDNESDAY, 8, 10, "Prof A");

    ResolutionReportView report = service.report();
    assertEquals(1, report.capacityViolations());
    assertFalse(report.conflictFree());
  }

  @Test
  void generateFacultyWideProducesAConflictFreeTimetable() {
    // Several rooms so the generator has room to spread sessions out.
    for (int i = 0; i < 6; i++) {
      rooms.save(new Room(0L, "G" + i, "G", 200, 1, false));
    }
    long other = courses.save(new Course(0L, "MAT", "Matematika", "2026LJ", 5, "", "")).id();
    // A pile of colliding sessions: same course/room/time and cross-room overlaps.
    slot(1L, DayOfWeek.MONDAY, 8, 10, "Prof A");
    slot(1L, DayOfWeek.MONDAY, 8, 10, "Prof A");
    slot(1L, DayOfWeek.MONDAY, 8, 10, "Prof B");
    schedule.save(
        new ClassSchedule(
            0L,
            other,
            null,
            GroupType.LECTURE,
            1L,
            DayOfWeek.MONDAY,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            "Prof A"));

    ResolutionReportView before = service.report();
    assertFalse(before.conflictFree());

    ResolutionReportView after = service.generateFacultyWide();
    assertTrue(after.conflictFree(), "faculty-wide generation should produce a conflict-free plan");
  }

  @Test
  void cleanScheduleIsConflictFree() {
    long r1 = rooms.save(new Room(0L, "RA", "R", 100, 1, false)).id();
    long r2 = rooms.save(new Room(0L, "RB", "R", 100, 1, false)).id();
    slot(r1, DayOfWeek.MONDAY, 8, 10, "Prof A");
    slot(r2, DayOfWeek.TUESDAY, 8, 10, "Prof B");

    ResolutionReportView report = service.report();
    assertTrue(report.conflictFree());
    assertEquals(0, report.roomCollisions());
  }
}
