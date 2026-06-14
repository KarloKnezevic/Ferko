package hr.fer.zemris.ferko.application.usecase.schedule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.GroupType;
import hr.fer.zemris.ferko.domain.model.Room;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CourseScheduleServiceTest {

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
  }

  private FakeSchedule schedule;
  private InMemoryAcademicRepositories.Rooms rooms;
  private InMemoryAcademicRepositories.Courses courses;
  private CourseScheduleService service;

  @BeforeEach
  void setUp() {
    schedule = new FakeSchedule();
    rooms = new InMemoryAcademicRepositories.Rooms();
    courses = new InMemoryAcademicRepositories.Courses();
    service = new CourseScheduleService(schedule, rooms, courses);
  }

  @Test
  void buildsSlotsWithRoomCodeGroupCodeAndCroatianDay() {
    long courseId = courses.save(courseStub()).id();
    long roomId = rooms.save(new Room(0L, "A101", "Zgrada A", 100, 2, false)).id();
    StudentGroup group =
        courses.addGroup(new StudentGroup(0L, courseId, "L01", GroupType.LAB, "Pon", 16));

    schedule.save(
        new ClassSchedule(
            0L,
            courseId,
            group.id(),
            GroupType.LAB,
            roomId,
            DayOfWeek.MONDAY,
            LocalTime.of(8, 0),
            LocalTime.of(10, 0),
            "Marko"));

    List<ScheduleSlotView> slots = service.forCourse(courseId);
    assertEquals(1, slots.size());
    ScheduleSlotView slot = slots.get(0);
    assertEquals("Ponedjeljak", slot.dayOfWeek());
    assertEquals("08:00", slot.startsAt());
    assertEquals("10:00", slot.endsAt());
    assertEquals("A101", slot.roomCode());
    assertEquals("L01", slot.groupCode());
    assertEquals("LAB", slot.type());
    assertEquals("Marko", slot.instructor());
  }

  @Test
  void handlesNullRoomAndGroup() {
    long courseId = courses.save(courseStub()).id();
    schedule.save(
        new ClassSchedule(
            0L,
            courseId,
            null,
            GroupType.LECTURE,
            null,
            DayOfWeek.WEDNESDAY,
            LocalTime.of(12, 0),
            LocalTime.of(14, 0),
            null));

    ScheduleSlotView slot = service.forCourse(courseId).get(0);
    assertEquals("Srijeda", slot.dayOfWeek());
    assertEquals("", slot.roomCode());
    assertEquals("", slot.groupCode());
    assertEquals("", slot.instructor());
    assertTrue(service.forCourse(999L).isEmpty());
  }

  private static hr.fer.zemris.ferko.domain.model.Course courseStub() {
    return new hr.fer.zemris.ferko.domain.model.Course(
        0L, "PROG", "Programiranje", "2025/26-ZS", 6, "", "");
  }
}
