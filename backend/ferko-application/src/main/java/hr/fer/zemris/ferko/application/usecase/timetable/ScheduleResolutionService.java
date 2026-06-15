package hr.fer.zemris.ferko.application.usecase.timetable;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.application.usecase.timetable.ScheduleResolutionViews.CollisionView;
import hr.fer.zemris.ferko.application.usecase.timetable.ScheduleResolutionViews.HeatCell;
import hr.fer.zemris.ferko.application.usecase.timetable.ScheduleResolutionViews.MoveSuggestionView;
import hr.fer.zemris.ferko.application.usecase.timetable.ScheduleResolutionViews.ResolutionReportView;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Room;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Faculty-wide hard-constraint collision detection and interactive resolution over the persisted
 * weekly timetable. Hard constraints (a conflict-free timetable violates none):
 *
 * <ul>
 *   <li><b>ROOM</b> — a room is never double-booked at overlapping times;
 *   <li><b>INSTRUCTOR</b> — an instructor never teaches two overlapping sessions;
 *   <li><b>GROUP</b> — a student group is never scheduled for two overlapping sessions (sessions of
 *       the same course that share a group, course-wide slots affecting every group);
 *   <li><b>CAPACITY</b> — a session's enrolment never exceeds its room's capacity.
 * </ul>
 *
 * <p>For every violation it proposes a concrete conflict-free move (a new weekday/time/room for one
 * session that introduces no new violation), which the UI can apply with one click; {@link
 * #autoResolve()} applies such moves greedily until the timetable is conflict-free or no further
 * move helps.
 */
public class ScheduleResolutionService {

  private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
  private static final List<DayOfWeek> WEEK =
      List.of(
          DayOfWeek.MONDAY,
          DayOfWeek.TUESDAY,
          DayOfWeek.WEDNESDAY,
          DayOfWeek.THURSDAY,
          DayOfWeek.FRIDAY);

  /** Candidate start times searched when proposing a move (08:00–18:00 on the hour). */
  private static final List<LocalTime> START_GRID = buildStartGrid();

  /** Upper bound on auto-resolve rounds, a safety net against pathological inputs. */
  private static final int MAX_AUTO_ROUNDS = 200;

  /** Cap on the number of detailed collisions (with suggestions) returned in one report. */
  private static final int MAX_DETAILED = 150;

  private final ClassScheduleRepository scheduleRepository;
  private final CourseRepository courseRepository;
  private final RoomRepository roomRepository;
  private final EnrollmentRepository enrollmentRepository;

  public ScheduleResolutionService(
      ClassScheduleRepository scheduleRepository,
      CourseRepository courseRepository,
      RoomRepository roomRepository,
      EnrollmentRepository enrollmentRepository) {
    this.scheduleRepository = scheduleRepository;
    this.courseRepository = courseRepository;
    this.roomRepository = roomRepository;
    this.enrollmentRepository = enrollmentRepository;
  }

  private static List<LocalTime> buildStartGrid() {
    List<LocalTime> grid = new ArrayList<>();
    for (int hour = 8; hour <= 18; hour++) {
      grid.add(LocalTime.of(hour, 0));
    }
    return grid;
  }

  /** The current faculty-wide collision report, with a suggested fix per collision. */
  public ResolutionReportView report() {
    return buildReport(new ArrayList<>(scheduleRepository.findAll()));
  }

  /**
   * Moves one session to a new weekday/time/room (its duration is preserved) and returns the
   * recomputed report. Used by the one-click "resolve" action.
   */
  public ResolutionReportView move(long slotId, String dayOfWeek, String startsAt, Long roomId) {
    ClassSchedule slot = scheduleRepository.findById(slotId).orElse(null);
    if (slot != null) {
      DayOfWeek day = DayOfWeek.valueOf(dayOfWeek);
      LocalTime start = LocalTime.parse(startsAt);
      LocalTime end = start.plus(Duration.between(slot.startsAt(), slot.endsAt()));
      scheduleRepository.updatePlacement(slotId, day, start, end, roomId);
    }
    return report();
  }

  /** Greedily applies suggested moves until the timetable is conflict-free or no move helps. */
  public ResolutionReportView autoResolve() {
    List<ClassSchedule> slots = new ArrayList<>(scheduleRepository.findAll());
    repair(slots, context());
    persist(slots);
    return buildReport(slots);
  }

  /**
   * Generates a fresh faculty-wide placement for every session (all courses, groups and years) and
   * then repairs any residual hard collisions. Each session keeps its course/group/type/duration
   * and is greedily first-fit into a conflict-free weekday/time/room (most-constrained sessions, by
   * enrolment then duration, placed first), after which {@link #repair} drives the remainder toward
   * zero. The aim is a conflict-free final timetable; the returned report states whether that was
   * reached and lists any sessions that could not be placed without a clash.
   */
  public ResolutionReportView generateFacultyWide() {
    Context context = context();
    List<ClassSchedule> events = new ArrayList<>(scheduleRepository.findAll());
    events.sort(
        java.util.Comparator.<ClassSchedule>comparingInt(e -> context.enrolled(e.courseId()))
            .reversed()
            .thenComparing(
                e -> Duration.between(e.startsAt(), e.endsAt()),
                java.util.Comparator.reverseOrder())
            .thenComparingLong(ClassSchedule::id));
    List<ClassSchedule> placed = new ArrayList<>();
    for (ClassSchedule event : events) {
      Placement spot = firstFit(event, placed, context);
      placed.add(withPlacement(event, spot));
    }
    repair(placed, context);
    persist(placed);
    return buildReport(placed);
  }

  /** Greedy repair: keep applying a suggested move to some collision until none helps. */
  private void repair(List<ClassSchedule> slots, Context context) {
    int rounds = 0;
    while (rounds++ < MAX_AUTO_ROUNDS) {
      List<Collision> collisions = detect(slots, context);
      if (collisions.isEmpty()) {
        break;
      }
      boolean progressed = false;
      for (Collision collision : collisions) {
        ClassSchedule slot = byId(slots, collision.slotId());
        Placement move = propose(slot, slots, context);
        if (move != null) {
          apply(slots, slot, move);
          progressed = true;
          break; // re-detect from a clean state after each applied move
        }
      }
      if (!progressed) {
        break; // remaining collisions have no feasible move
      }
    }
  }

  /** Persists the (possibly repositioned) placement of every slot. */
  private void persist(List<ClassSchedule> slots) {
    for (ClassSchedule slot : slots) {
      scheduleRepository.updatePlacement(
          slot.id(), slot.dayOfWeek(), slot.startsAt(), slot.endsAt(), slot.roomId());
    }
  }

  /** First conflict-free weekday/time/room for an event against the already-placed set. */
  private Placement firstFit(ClassSchedule event, List<ClassSchedule> placed, Context context) {
    Duration duration = Duration.between(event.startsAt(), event.endsAt());
    int enrolled = context.enrolled(event.courseId());
    Map<DayOfWeek, List<ClassSchedule>> byDay = new HashMap<>();
    for (ClassSchedule slot : placed) {
      byDay.computeIfAbsent(slot.dayOfWeek(), key -> new ArrayList<>()).add(slot);
    }
    for (DayOfWeek day : WEEK) {
      List<ClassSchedule> dayslots = byDay.getOrDefault(day, List.of());
      for (LocalTime start : START_GRID) {
        LocalTime end = start.plus(duration);
        if (end.isAfter(LocalTime.of(22, 0))) {
          continue;
        }
        for (Room room : context.rooms()) {
          if (room.capacity() < enrolled) {
            continue;
          }
          if (isFree(event, dayslots, start, end, room.id())) {
            return new Placement(day, start, end, room.id());
          }
        }
      }
    }
    // No conflict-free spot: keep the event's current placement and let repair try later.
    return new Placement(event.dayOfWeek(), event.startsAt(), event.endsAt(), event.roomId());
  }

  private static ClassSchedule withPlacement(ClassSchedule event, Placement spot) {
    return new ClassSchedule(
        event.id(),
        event.courseId(),
        event.groupId(),
        event.type(),
        spot.roomId(),
        spot.day(),
        spot.start(),
        spot.end(),
        event.instructor());
  }

  // ----- detection -------------------------------------------------------------------------------

  private ResolutionReportView buildReport(List<ClassSchedule> slots) {
    Context context = context();
    List<Collision> collisions = detect(slots, context);
    int room = 0;
    int instructor = 0;
    int group = 0;
    int capacity = 0;
    // Aggregate EVERY collision (not just the capped detailed subset) into (room, weekday, kind)
    // buckets. A UI built from this heatmap therefore always sums back to the per-kind counters.
    Map<HeatKey, Integer> heat = new LinkedHashMap<>();
    List<CollisionView> views = new ArrayList<>();
    for (Collision collision : collisions) {
      switch (collision.kind()) {
        case "ROOM" -> room++;
        case "INSTRUCTOR" -> instructor++;
        case "GROUP" -> group++;
        default -> capacity++;
      }
      ClassSchedule slot = byId(slots, collision.slotId());
      if (slot != null) {
        HeatKey key =
            new HeatKey(context.roomCode(slot.roomId()), slot.dayOfWeek().name(), collision.kind());
        heat.merge(key, 1, Integer::sum);
      }
      if (views.size() < MAX_DETAILED) {
        views.add(toView(collision, slots, context));
      }
    }
    List<HeatCell> heatmap = new ArrayList<>();
    heat.forEach(
        (key, count) -> heatmap.add(new HeatCell(key.room(), key.day(), key.kind(), count)));
    int total = room + instructor + group + capacity;
    return new ResolutionReportView(
        slots.size(),
        room,
        instructor,
        group,
        capacity,
        total,
        collisions.isEmpty(),
        heatmap,
        views);
  }

  private List<Collision> detect(List<ClassSchedule> slots, Context context) {
    List<Collision> collisions = new ArrayList<>();
    for (int i = 0; i < slots.size(); i++) {
      ClassSchedule a = slots.get(i);
      // Capacity is a per-slot violation.
      if (a.roomId() != null) {
        int capacity = context.roomCapacity(a.roomId());
        int enrolled = context.enrolled(a.courseId());
        if (enrolled > capacity) {
          collisions.add(new Collision("CAPACITY", a.id(), null));
        }
      }
      for (int j = i + 1; j < slots.size(); j++) {
        ClassSchedule b = slots.get(j);
        if (!overlaps(a, b)) {
          continue;
        }
        if (a.roomId() != null && Objects.equals(a.roomId(), b.roomId())) {
          collisions.add(new Collision("ROOM", b.id(), a.id()));
        }
        if (sameInstructor(a, b)) {
          collisions.add(new Collision("INSTRUCTOR", b.id(), a.id()));
        }
        if (sameGroup(a, b)) {
          collisions.add(new Collision("GROUP", b.id(), a.id()));
        }
      }
    }
    return collisions;
  }

  private static boolean overlaps(ClassSchedule a, ClassSchedule b) {
    return a.dayOfWeek() == b.dayOfWeek()
        && a.startsAt().isBefore(b.endsAt())
        && b.startsAt().isBefore(a.endsAt());
  }

  private static boolean sameInstructor(ClassSchedule a, ClassSchedule b) {
    return a.instructor() != null
        && !a.instructor().isBlank()
        && a.instructor().equalsIgnoreCase(b.instructor());
  }

  /** Two same-course overlapping sessions clash for a group when they share one (null = all). */
  private static boolean sameGroup(ClassSchedule a, ClassSchedule b) {
    if (a.courseId() != b.courseId()) {
      return false;
    }
    return a.groupId() == null || b.groupId() == null || Objects.equals(a.groupId(), b.groupId());
  }

  // ----- suggestion ------------------------------------------------------------------------------

  /** Proposes a conflict-free placement for {@code slot}, or {@code null} when none is found. */
  private Placement propose(ClassSchedule slot, List<ClassSchedule> slots, Context context) {
    if (slot == null) {
      return null;
    }
    Duration duration = Duration.between(slot.startsAt(), slot.endsAt());
    int enrolled = context.enrolled(slot.courseId());
    // Index by weekday once so candidate feasibility only scans the relevant day's slots.
    Map<DayOfWeek, List<ClassSchedule>> byDay = new HashMap<>();
    for (ClassSchedule s : slots) {
      byDay.computeIfAbsent(s.dayOfWeek(), key -> new ArrayList<>()).add(s);
    }
    // Prefer keeping the original weekday/time and only changing room; then widen the search.
    List<Room> rooms = context.rooms();
    for (DayOfWeek day : orderedDays(slot.dayOfWeek())) {
      List<ClassSchedule> dayslots = byDay.getOrDefault(day, List.of());
      for (LocalTime start : orderedStarts(slot.startsAt())) {
        LocalTime end = start.plus(duration);
        if (end.isAfter(LocalTime.of(22, 0))) {
          continue;
        }
        for (Room room : rooms) {
          if (room.capacity() < enrolled) {
            continue;
          }
          if (day == slot.dayOfWeek()
              && start.equals(slot.startsAt())
              && Objects.equals(room.id(), slot.roomId())) {
            continue; // that is the current (colliding) placement
          }
          if (isFree(slot, dayslots, start, end, room.id())) {
            return new Placement(day, start, end, room.id());
          }
        }
      }
    }
    return null;
  }

  /** True when placing {@code slot} at the candidate introduces no room/instructor/group clash. */
  private boolean isFree(
      ClassSchedule slot,
      List<ClassSchedule> dayslots,
      LocalTime start,
      LocalTime end,
      Long roomId) {
    for (ClassSchedule other : dayslots) {
      if (other.id() == slot.id()) {
        continue;
      }
      if (!(start.isBefore(other.endsAt()) && other.startsAt().isBefore(end))) {
        continue;
      }
      if (roomId != null && Objects.equals(roomId, other.roomId())) {
        return false;
      }
      if (slot.instructor() != null
          && !slot.instructor().isBlank()
          && slot.instructor().equalsIgnoreCase(other.instructor())) {
        return false;
      }
      if (slot.courseId() == other.courseId()
          && (slot.groupId() == null
              || other.groupId() == null
              || Objects.equals(slot.groupId(), other.groupId()))) {
        return false;
      }
    }
    return true;
  }

  private List<DayOfWeek> orderedDays(DayOfWeek preferred) {
    List<DayOfWeek> days = new ArrayList<>();
    days.add(preferred);
    for (DayOfWeek day : WEEK) {
      if (day != preferred) {
        days.add(day);
      }
    }
    return days;
  }

  private List<LocalTime> orderedStarts(LocalTime preferred) {
    List<LocalTime> starts = new ArrayList<>();
    starts.add(preferred);
    for (LocalTime start : START_GRID) {
      if (!start.equals(preferred)) {
        starts.add(start);
      }
    }
    return starts;
  }

  private static void apply(List<ClassSchedule> slots, ClassSchedule slot, Placement move) {
    int index = slots.indexOf(slot);
    slots.set(
        index,
        new ClassSchedule(
            slot.id(),
            slot.courseId(),
            slot.groupId(),
            slot.type(),
            move.roomId(),
            move.day(),
            move.start(),
            move.end(),
            slot.instructor()));
  }

  // ----- view mapping ----------------------------------------------------------------------------

  private CollisionView toView(Collision collision, List<ClassSchedule> slots, Context context) {
    ClassSchedule slot = byId(slots, collision.slotId());
    ClassSchedule other =
        collision.otherSlotId() == null ? null : byId(slots, collision.otherSlotId());
    Placement move = propose(slot, slots, context);
    MoveSuggestionView suggestion =
        move == null
            ? new MoveSuggestionView(false, null, null, null, null, null, "Nema slobodnog termina")
            : new MoveSuggestionView(
                true,
                move.day().name(),
                move.start().format(HM),
                move.end().format(HM),
                move.roomId(),
                context.roomCode(move.roomId()),
                "Premjesti u " + context.roomCode(move.roomId()));
    return new CollisionView(
        collision.kind(),
        slot.dayOfWeek().name(),
        slot.startsAt().format(HM),
        slot.endsAt().format(HM),
        resourceLabel(collision, slot, context),
        context.roomCode(slot.roomId()),
        slot.id(),
        label(slot, context),
        collision.otherSlotId(),
        other == null ? null : label(other, context),
        suggestion);
  }

  private String resourceLabel(Collision collision, ClassSchedule slot, Context context) {
    return switch (collision.kind()) {
      case "INSTRUCTOR" -> slot.instructor() == null ? "" : slot.instructor();
      case "GROUP" -> context.courseCode(slot.courseId()) + " " + context.groupLabel(slot);
      default -> context.roomCode(slot.roomId());
    };
  }

  private String label(ClassSchedule slot, Context context) {
    String room = slot.roomId() == null ? "—" : context.roomCode(slot.roomId());
    return context.courseCode(slot.courseId()) + " " + context.groupLabel(slot) + " @ " + room;
  }

  private static ClassSchedule byId(List<ClassSchedule> slots, long id) {
    for (ClassSchedule slot : slots) {
      if (slot.id() == id) {
        return slot;
      }
    }
    return null;
  }

  private Context context() {
    Map<Long, Room> rooms = new HashMap<>();
    roomRepository.findAll().forEach(room -> rooms.put(room.id(), room));
    Map<Long, Course> courses = new HashMap<>();
    courseRepository.findAll().forEach(course -> courses.put(course.id(), course));
    return new Context(rooms, courses, new HashMap<>(), new HashMap<>());
  }

  /** Cached lookups shared across a single report/auto-resolve pass. */
  private final class Context {
    private final Map<Long, Room> rooms;
    private final Map<Long, Course> courses;
    private final Map<Long, Integer> enrolledByCourse;
    private final Map<Long, String> groupCodeById;

    private Context(
        Map<Long, Room> rooms,
        Map<Long, Course> courses,
        Map<Long, Integer> enrolledByCourse,
        Map<Long, String> groupCodeById) {
      this.rooms = rooms;
      this.courses = courses;
      this.enrolledByCourse = enrolledByCourse;
      this.groupCodeById = groupCodeById;
    }

    List<Room> rooms() {
      return List.copyOf(rooms.values());
    }

    int roomCapacity(Long roomId) {
      Room room = roomId == null ? null : rooms.get(roomId);
      return room == null ? Integer.MAX_VALUE : room.capacity();
    }

    String roomCode(Long roomId) {
      Room room = roomId == null ? null : rooms.get(roomId);
      return room == null ? "—" : room.code();
    }

    String courseCode(long courseId) {
      Course course = courses.get(courseId);
      return course == null ? ("#" + courseId) : course.code();
    }

    int enrolled(long courseId) {
      return enrolledByCourse.computeIfAbsent(
          courseId, id -> enrollmentRepository.findByCourse(id).size());
    }

    String groupLabel(ClassSchedule slot) {
      if (slot.groupId() == null) {
        return "(svi)";
      }
      return groupCodeById.computeIfAbsent(
          slot.groupId(),
          id ->
              courseRepository.findGroupsByCourse(slot.courseId()).stream()
                  .filter(group -> group.id() == id)
                  .map(StudentGroup::groupCode)
                  .findFirst()
                  .orElse("grupa"));
    }
  }

  private record Collision(String kind, long slotId, Long otherSlotId) {}

  /** Aggregation key for the uncapped collision heatmap: room code, weekday name and kind. */
  private record HeatKey(String room, String day, String kind) {}

  private record Placement(DayOfWeek day, LocalTime start, LocalTime end, Long roomId) {}
}
