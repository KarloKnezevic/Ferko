package hr.fer.zemris.ferko.application.usecase.timetable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.CollisionReportView;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.GroupType;
import hr.fer.zemris.ferko.domain.model.Room;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TimetableServiceTest {

  private final InMemoryAcademicRepositories.Courses courses =
      new InMemoryAcademicRepositories.Courses();
  private final InMemoryAcademicRepositories.Rooms rooms = new InMemoryAcademicRepositories.Rooms();
  private final FakeSchedule schedule = new FakeSchedule();
  private final TimetableService service = new TimetableService(schedule, courses, rooms);

  @Test
  void detectsRoomAndInstructorConflicts() {
    long a = courses.save(new Course(0L, "A", "Algebra", "2026LJ", 5, "", "")).id();
    long b = courses.save(new Course(0L, "B", "Baze", "2026LJ", 5, "", "")).id();
    long c = courses.save(new Course(0L, "C", "Cyber", "2026LJ", 5, "", "")).id();
    long room1 = rooms.save(new Room(0L, "D272", "D", 120, 2, false)).id();
    long room2 = rooms.save(new Room(0L, "A105", "A", 20, 1, true)).id();

    // A and B share room1 on Monday with overlapping times -> 1 room conflict.
    schedule.save(slot(a, room1, DayOfWeek.MONDAY, 10, 12, "Prof X"));
    schedule.save(slot(b, room1, DayOfWeek.MONDAY, 11, 13, "Prof Y"));
    // A and C share instructor "Prof X" on Monday with overlapping times -> 1 instructor conflict.
    schedule.save(slot(c, room2, DayOfWeek.MONDAY, 10, 12, "Prof X"));

    CollisionReportView report = service.collisions();

    assertEquals(3, report.totalSlots());
    assertEquals(1, report.roomConflicts());
    assertEquals(1, report.instructorConflicts());
    assertTrue(report.conflicts().stream().anyMatch(conflict -> conflict.kind().equals("ROOM")));
    assertTrue(
        report.conflicts().stream().anyMatch(conflict -> conflict.kind().equals("INSTRUCTOR")));
  }

  @Test
  void weeklyExposesAllSlotsWithCodes() {
    long a = courses.save(new Course(0L, "A", "Algebra", "2026LJ", 5, "", "")).id();
    long room1 = rooms.save(new Room(0L, "D272", "D", 120, 2, false)).id();
    schedule.save(slot(a, room1, DayOfWeek.TUESDAY, 8, 10, "Prof X"));

    var weekly = service.weekly();
    assertEquals(1, weekly.size());
    assertEquals("A", weekly.get(0).courseCode());
    assertEquals("D272", weekly.get(0).room());
    assertEquals("08:00", weekly.get(0).startsAt());
  }

  @Test
  void noConflictForTouchingDifferentDayOrNullRoom() {
    long a = courses.save(new Course(0L, "A", "Algebra", "2026LJ", 5, "", "")).id();
    long b = courses.save(new Course(0L, "B", "Baze", "2026LJ", 5, "", "")).id();
    long room1 = rooms.save(new Room(0L, "D272", "D", 120, 2, false)).id();

    // Back-to-back in the same room (10–12 then 12–14) is not an overlap.
    schedule.save(slot(a, room1, DayOfWeek.MONDAY, 10, 12, "Prof X"));
    schedule.save(slot(b, room1, DayOfWeek.MONDAY, 12, 14, "Prof Y"));
    // Same room, overlapping time, but different days -> not a conflict.
    schedule.save(slot(a, room1, DayOfWeek.TUESDAY, 10, 12, "Prof X"));
    schedule.save(slot(b, room1, DayOfWeek.WEDNESDAY, 10, 12, "Prof Y"));
    // Overlapping time, no room and no instructor -> nothing to clash on.
    schedule.save(
        new ClassSchedule(
            0L,
            a,
            null,
            GroupType.LECTURE,
            null,
            DayOfWeek.MONDAY,
            LocalTime.of(10, 0),
            LocalTime.of(12, 0),
            ""));

    CollisionReportView report = service.collisions();
    assertEquals(0, report.roomConflicts());
    assertEquals(0, report.instructorConflicts());
  }

  private static ClassSchedule slot(
      long courseId, long roomId, DayOfWeek day, int startHour, int endHour, String instructor) {
    return new ClassSchedule(
        0L,
        courseId,
        null,
        GroupType.LECTURE,
        roomId,
        day,
        LocalTime.of(startHour, 0),
        LocalTime.of(endHour, 0),
        instructor);
  }

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
}
