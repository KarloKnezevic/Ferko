package hr.fer.zemris.ferko.application.usecase.schedule;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Room;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Builds the weekly teaching timetable ("Raspored nastave") for a single course. */
public class CourseScheduleService {

  private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
  private static final Map<String, String> DAY_HR =
      Map.of(
          "MONDAY", "Ponedjeljak",
          "TUESDAY", "Utorak",
          "WEDNESDAY", "Srijeda",
          "THURSDAY", "Četvrtak",
          "FRIDAY", "Petak",
          "SATURDAY", "Subota",
          "SUNDAY", "Nedjelja");

  private final ClassScheduleRepository scheduleRepository;
  private final RoomRepository roomRepository;
  private final CourseRepository courseRepository;

  public CourseScheduleService(
      ClassScheduleRepository scheduleRepository,
      RoomRepository roomRepository,
      CourseRepository courseRepository) {
    this.scheduleRepository = scheduleRepository;
    this.roomRepository = roomRepository;
    this.courseRepository = courseRepository;
  }

  public List<ScheduleSlotView> forCourse(long courseId) {
    Map<Long, String> groupCodeById =
        courseRepository.findGroupsByCourse(courseId).stream()
            .collect(Collectors.toMap(StudentGroup::id, StudentGroup::groupCode, (a, b) -> a));
    return scheduleRepository.findByCourse(courseId).stream()
        .map(slot -> toView(slot, groupCodeById))
        .toList();
  }

  private ScheduleSlotView toView(ClassSchedule slot, Map<Long, String> groupCodeById) {
    String day = slot.dayOfWeek().name();
    String roomCode =
        slot.roomId() == null
            ? ""
            : roomRepository.findById(slot.roomId()).map(Room::code).orElse("");
    String groupCode = slot.groupId() == null ? "" : groupCodeById.getOrDefault(slot.groupId(), "");
    return new ScheduleSlotView(
        slot.id(),
        DAY_HR.getOrDefault(day, day),
        slot.startsAt().format(HM),
        slot.endsAt().format(HM),
        slot.type().name(),
        groupCode,
        roomCode,
        slot.instructor() == null ? "" : slot.instructor());
  }
}
