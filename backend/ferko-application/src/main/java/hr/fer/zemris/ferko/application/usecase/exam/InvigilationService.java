package hr.fer.zemris.ferko.application.usecase.exam;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.ExamAssistantRepository;
import hr.fer.zemris.ferko.application.port.ExamRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamRoomAssistant;
import hr.fer.zemris.ferko.domain.model.Room;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The signed-in staff member's invigilation duties ("Moja dežurstva"): every exam room they are
 * assigned to supervise, with exam, course and room details.
 */
public class InvigilationService {

  private final ExamAssistantRepository assistantRepository;
  private final ExamRepository examRepository;
  private final CourseRepository courseRepository;
  private final RoomRepository roomRepository;
  private final AppUserRepository userRepository;

  public InvigilationService(
      ExamAssistantRepository assistantRepository,
      ExamRepository examRepository,
      CourseRepository courseRepository,
      RoomRepository roomRepository,
      AppUserRepository userRepository) {
    this.assistantRepository = assistantRepository;
    this.examRepository = examRepository;
    this.courseRepository = courseRepository;
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
  }

  /** Returns an empty list when the user is unknown or has no duties. */
  public List<MyDutyView> myDuties(String username) {
    return userRepository
        .findByUsername(username)
        .map(user -> assistantRepository.findByUser(user.id()).stream().map(this::toView).toList())
        .orElseGet(List::of)
        .stream()
        .sorted(
            Comparator.comparing(
                    MyDutyView::startsAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingLong(MyDutyView::examId))
        .toList();
  }

  private MyDutyView toView(ExamRoomAssistant assignment) {
    Optional<Exam> exam = examRepository.findById(assignment.examId());
    String title = exam.map(Exam::title).orElse("");
    String shortName = exam.map(Exam::shortName).orElse("");
    long courseId = exam.map(Exam::courseId).orElse(0L);
    boolean published = exam.map(Exam::published).orElse(false);
    Optional<Course> course = courseRepository.findById(courseId);
    String roomCode = roomRepository.findById(assignment.roomId()).map(Room::code).orElse("");
    return new MyDutyView(
        assignment.examId(),
        title,
        shortName,
        courseId,
        course.map(Course::code).orElse(""),
        course.map(Course::name).orElse(""),
        exam.map(Exam::startsAt).orElse(null),
        roomCode,
        published);
  }
}
