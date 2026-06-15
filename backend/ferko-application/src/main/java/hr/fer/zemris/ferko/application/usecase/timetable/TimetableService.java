package hr.fer.zemris.ferko.application.usecase.timetable;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.CollisionReportView;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.ConflictView;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.OverCapacityView;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.RoomHeatView;
import hr.fer.zemris.ferko.application.usecase.timetable.TimetableViews.TimetableSlotView;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Room;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Builds the faculty-wide weekly teaching timetable and detects collisions — two slots overlapping
 * in time on the same weekday that compete for the same room or the same instructor. Student-level
 * conflict minimisation is the job of the scheduling engine; this service reports the hard resource
 * clashes in whatever timetable currently exists.
 */
public class TimetableService {

  private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

  private final ClassScheduleRepository scheduleRepository;
  private final CourseRepository courseRepository;
  private final RoomRepository roomRepository;
  private final EnrollmentRepository enrollmentRepository;

  public TimetableService(
      ClassScheduleRepository scheduleRepository,
      CourseRepository courseRepository,
      RoomRepository roomRepository,
      EnrollmentRepository enrollmentRepository) {
    this.scheduleRepository = scheduleRepository;
    this.courseRepository = courseRepository;
    this.roomRepository = roomRepository;
    this.enrollmentRepository = enrollmentRepository;
  }

  /** The full weekly timetable, ordered by day then start time. */
  public List<TimetableSlotView> weekly() {
    Map<Long, Course> courses = indexCourses();
    Map<Long, String> roomCodes = indexRoomCodes();
    return scheduleRepository.findAll().stream()
        .map(slot -> toView(slot, courses, roomCodes))
        .toList();
  }

  /** The collision report across the whole timetable. */
  public CollisionReportView collisions() {
    List<ClassSchedule> slots = scheduleRepository.findAll();
    Map<Long, Course> courses = indexCourses();
    Map<Long, Room> roomsById = indexRooms();
    Map<Long, String> roomCodes =
        roomsById.values().stream().collect(Collectors.toMap(Room::id, Room::code));

    List<ConflictView> conflicts = new ArrayList<>();
    int roomConflicts =
        collectConflicts(
            slots,
            "ROOM",
            slot -> slot.roomId() == null ? null : "room#" + slot.roomId(),
            slot -> slot.roomId() == null ? "" : roomCodes.getOrDefault(slot.roomId(), ""),
            courses,
            conflicts);
    int instructorConflicts =
        collectConflicts(
            slots,
            "INSTRUCTOR",
            slot -> blankToNull(slot.instructor()),
            slot -> slot.instructor() == null ? "" : slot.instructor(),
            courses,
            conflicts);

    Map<Long, Integer> enrolledByCourse = enrolledByCourse(slots);
    return new CollisionReportView(
        slots.size(),
        roomConflicts,
        instructorConflicts,
        conflicts,
        roomUtilization(slots, roomCodes),
        overCapacity(slots, courses, roomsById, enrolledByCourse),
        heatmap(slots, roomsById, enrolledByCourse));
  }

  /** Enrolled-student count per course that appears in the timetable (queried once per course). */
  private Map<Long, Integer> enrolledByCourse(List<ClassSchedule> slots) {
    Map<Long, Integer> enrolled = new java.util.HashMap<>();
    for (ClassSchedule slot : slots) {
      enrolled.computeIfAbsent(slot.courseId(), id -> enrollmentRepository.findByCourse(id).size());
    }
    return enrolled;
  }

  /** Slots whose enrolled course exceeds the assigned room capacity. */
  private List<OverCapacityView> overCapacity(
      List<ClassSchedule> slots,
      Map<Long, Course> courses,
      Map<Long, Room> roomsById,
      Map<Long, Integer> enrolledByCourse) {
    List<OverCapacityView> result = new ArrayList<>();
    for (ClassSchedule slot : slots) {
      if (slot.roomId() == null) {
        continue;
      }
      Room room = roomsById.get(slot.roomId());
      if (room == null) {
        continue;
      }
      int enrolled = enrolledByCourse.getOrDefault(slot.courseId(), 0);
      if (enrolled > room.capacity()) {
        Course course = courses.get(slot.courseId());
        result.add(
            new OverCapacityView(
                course == null ? "" : course.code(),
                course == null ? "" : course.name(),
                room.code(),
                slot.dayOfWeek().name(),
                slot.startsAt().format(HM),
                slot.endsAt().format(HM),
                enrolled,
                room.capacity()));
      }
    }
    result.sort(
        (a, b) -> Integer.compare(b.enrolled() - b.capacity(), a.enrolled() - a.capacity()));
    return result;
  }

  /** Per-room weekday load (Mon..Fri) with an over-capacity flag, for the heatmap. */
  private List<RoomHeatView> heatmap(
      List<ClassSchedule> slots, Map<Long, Room> roomsById, Map<Long, Integer> enrolledByCourse) {
    Map<Long, int[]> perDayByRoom = new LinkedHashMap<>();
    Map<Long, Boolean> overByRoom = new LinkedHashMap<>();
    for (ClassSchedule slot : slots) {
      if (slot.roomId() == null) {
        continue;
      }
      int dayIndex = slot.dayOfWeek().getValue() - 1; // Monday=0 .. Sunday=6
      if (dayIndex < 0 || dayIndex > 4) {
        continue; // heatmap covers the working week
      }
      perDayByRoom.computeIfAbsent(slot.roomId(), id -> new int[5])[dayIndex]++;
      Room room = roomsById.get(slot.roomId());
      boolean over =
          room != null && enrolledByCourse.getOrDefault(slot.courseId(), 0) > room.capacity();
      overByRoom.merge(slot.roomId(), over, (a, b) -> a || b);
    }
    List<RoomHeatView> heat = new ArrayList<>();
    for (Map.Entry<Long, int[]> entry : perDayByRoom.entrySet()) {
      Room room = roomsById.get(entry.getKey());
      int[] perDay = entry.getValue();
      int total = 0;
      List<Integer> days = new ArrayList<>(5);
      for (int count : perDay) {
        days.add(count);
        total += count;
      }
      heat.add(
          new RoomHeatView(
              room == null ? "" : room.code(),
              room == null ? 0 : room.capacity(),
              days,
              total,
              overByRoom.getOrDefault(entry.getKey(), false)));
    }
    heat.sort((a, b) -> Integer.compare(b.total(), a.total()));
    return heat;
  }

  /** Busiest rooms first: weekly slot count per room. */
  private static List<TimetableViews.RoomUsageView> roomUtilization(
      List<ClassSchedule> slots, Map<Long, String> roomCodes) {
    Map<Long, Integer> countByRoom = new java.util.HashMap<>();
    for (ClassSchedule slot : slots) {
      if (slot.roomId() != null) {
        countByRoom.merge(slot.roomId(), 1, Integer::sum);
      }
    }
    return countByRoom.entrySet().stream()
        .map(
            entry ->
                new TimetableViews.RoomUsageView(
                    roomCodes.getOrDefault(entry.getKey(), ""), entry.getValue()))
        .sorted((a, b) -> Integer.compare(b.slots(), a.slots()))
        .toList();
  }

  private int collectConflicts(
      List<ClassSchedule> slots,
      String kind,
      Function<ClassSchedule, String> groupKey,
      Function<ClassSchedule, String> resourceLabel,
      Map<Long, Course> courses,
      List<ConflictView> sink) {
    Map<String, List<ClassSchedule>> byResource =
        slots.stream()
            .filter(slot -> groupKey.apply(slot) != null)
            .collect(Collectors.groupingBy(groupKey));
    int count = 0;
    for (List<ClassSchedule> group : byResource.values()) {
      for (int i = 0; i < group.size(); i++) {
        for (int j = i + 1; j < group.size(); j++) {
          ClassSchedule a = group.get(i);
          ClassSchedule b = group.get(j);
          if (a.courseId() != b.courseId() && overlaps(a, b)) {
            count++;
            sink.add(
                new ConflictView(
                    kind,
                    resourceLabel.apply(a),
                    a.dayOfWeek().name(),
                    laterStart(a, b).format(HM),
                    earlierEnd(a, b).format(HM),
                    courseLabel(courses, a.courseId()),
                    courseLabel(courses, b.courseId())));
          }
        }
      }
    }
    return count;
  }

  private static boolean overlaps(ClassSchedule a, ClassSchedule b) {
    return a.dayOfWeek() == b.dayOfWeek()
        && a.startsAt().isBefore(b.endsAt())
        && b.startsAt().isBefore(a.endsAt());
  }

  private static java.time.LocalTime laterStart(ClassSchedule a, ClassSchedule b) {
    return a.startsAt().isAfter(b.startsAt()) ? a.startsAt() : b.startsAt();
  }

  private static java.time.LocalTime earlierEnd(ClassSchedule a, ClassSchedule b) {
    return a.endsAt().isBefore(b.endsAt()) ? a.endsAt() : b.endsAt();
  }

  private Map<Long, Course> indexCourses() {
    return courseRepository.findAll().stream()
        .collect(Collectors.toMap(Course::id, Function.identity(), (left, right) -> left));
  }

  private Map<Long, String> indexRoomCodes() {
    return roomRepository.findAll().stream()
        .collect(Collectors.toMap(Room::id, Room::code, (left, right) -> left));
  }

  private Map<Long, Room> indexRooms() {
    return roomRepository.findAll().stream()
        .collect(Collectors.toMap(Room::id, Function.identity(), (left, right) -> left));
  }

  private TimetableSlotView toView(
      ClassSchedule slot, Map<Long, Course> courses, Map<Long, String> roomCodes) {
    Course course = courses.get(slot.courseId());
    return new TimetableSlotView(
        slot.courseId(),
        course == null ? "" : course.code(),
        course == null ? "" : course.name(),
        slot.type().name(),
        slot.dayOfWeek().name(),
        slot.startsAt().format(HM),
        slot.endsAt().format(HM),
        slot.roomId() == null ? "" : roomCodes.getOrDefault(slot.roomId(), ""),
        slot.instructor() == null ? "" : slot.instructor());
  }

  private static String courseLabel(Map<Long, Course> courses, long courseId) {
    Course course = courses.get(courseId);
    return course == null ? ("#" + courseId) : course.name();
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
