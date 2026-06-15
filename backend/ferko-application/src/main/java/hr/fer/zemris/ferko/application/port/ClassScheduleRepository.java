package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import java.util.List;

/** Persistence port for the weekly teaching timetable ("raspored nastave"). */
public interface ClassScheduleRepository {

  ClassSchedule save(ClassSchedule entry);

  List<ClassSchedule> findByCourse(long courseId);

  /** All timetable slots across every course (used for faculty-wide views and collision checks). */
  List<ClassSchedule> findAll();

  /** Removes every slot of a course (used when replacing it with a generated timetable). */
  int deleteByCourse(long courseId);
}
