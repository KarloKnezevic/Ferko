package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/** Persistence port for the weekly teaching timetable ("raspored nastave"). */
public interface ClassScheduleRepository {

  ClassSchedule save(ClassSchedule entry);

  List<ClassSchedule> findByCourse(long courseId);

  /** All timetable slots across every course (used for faculty-wide views and collision checks). */
  List<ClassSchedule> findAll();

  /** Single slot by id. */
  Optional<ClassSchedule> findById(long id);

  /**
   * Repositions a single slot (its weekday, time span and room) without touching its course/group;
   * used by interactive collision resolution. Returns {@code true} when a row was updated.
   */
  boolean updatePlacement(
      long id, DayOfWeek dayOfWeek, LocalTime startsAt, LocalTime endsAt, Long roomId);

  /** Removes every slot of a course (used when replacing it with a generated timetable). */
  int deleteByCourse(long courseId);
}
