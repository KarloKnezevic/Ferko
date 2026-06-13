package hr.fer.zemris.ferko.application.usecase.calendar;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.ExamRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Builds the current user's calendar by aggregating the weekly teaching timetable and dated
 * assessments across the courses they attend (as a student) or teach (as staff).
 */
public class CalendarService {

  private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

  private final AppUserRepository userRepository;
  private final StudentRepository studentRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;
  private final ExamRepository examRepository;
  private final ClassScheduleRepository scheduleRepository;
  private final RoomRepository roomRepository;

  public CalendarService(
      AppUserRepository userRepository,
      StudentRepository studentRepository,
      EnrollmentRepository enrollmentRepository,
      CourseRepository courseRepository,
      ExamRepository examRepository,
      ClassScheduleRepository scheduleRepository,
      RoomRepository roomRepository) {
    this.userRepository = userRepository;
    this.studentRepository = studentRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.courseRepository = courseRepository;
    this.examRepository = examRepository;
    this.scheduleRepository = scheduleRepository;
    this.roomRepository = roomRepository;
  }

  public CalendarView forUser(String username) {
    Optional<AppUser> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      return new CalendarView(List.of(), List.of());
    }
    long userId = user.get().id();
    Set<Long> courseIds = coursesFor(userId);

    List<CalendarView.WeeklySlot> weekly = new ArrayList<>();
    List<CalendarView.UpcomingExam> exams = new ArrayList<>();
    for (long courseId : courseIds) {
      Course course = courseRepository.findById(courseId).orElse(null);
      if (course == null) {
        continue;
      }
      for (ClassSchedule slot : scheduleRepository.findByCourse(courseId)) {
        weekly.add(
            new CalendarView.WeeklySlot(
                slot.dayOfWeek().name(),
                slot.startsAt().format(TIME),
                slot.endsAt().format(TIME),
                slot.type().name(),
                course.code(),
                course.name(),
                roomCode(slot.roomId()),
                slot.instructor()));
      }
      for (Exam exam : examRepository.findByCourse(courseId)) {
        if (exam.startsAt() != null) {
          exams.add(
              new CalendarView.UpcomingExam(
                  exam.startsAt(),
                  exam.title(),
                  exam.shortName(),
                  course.code(),
                  course.name(),
                  exam.durationMinutes()));
        }
      }
    }

    weekly.sort(
        Comparator.comparing(CalendarView.WeeklySlot::dayOfWeek)
            .thenComparing(CalendarView.WeeklySlot::startsAt));
    exams.sort(Comparator.comparing(CalendarView.UpcomingExam::startsAt));
    return new CalendarView(weekly, exams);
  }

  private Set<Long> coursesFor(long userId) {
    Set<Long> courseIds = new LinkedHashSet<>();
    Optional<Student> student = studentRepository.findByUserId(userId);
    if (student.isPresent()) {
      enrollmentRepository.findByStudent(student.get().id()).stream()
          .map(e -> e.courseId())
          .forEach(courseIds::add);
      return courseIds;
    }
    // Staff/admin: courses where this user is assigned as teaching staff.
    for (Course course : courseRepository.findAll()) {
      boolean teaches =
          courseRepository.findStaffByCourse(course.id()).stream()
              .anyMatch(s -> s.userId() == userId);
      if (teaches) {
        courseIds.add(course.id());
      }
    }
    return courseIds;
  }

  private String roomCode(Long roomId) {
    if (roomId == null) {
      return null;
    }
    return roomRepository.findById(roomId).map(r -> r.code()).orElse(null);
  }
}
