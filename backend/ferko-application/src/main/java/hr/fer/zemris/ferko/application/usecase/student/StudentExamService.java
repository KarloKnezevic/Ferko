package hr.fer.zemris.ferko.application.usecase.student;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.ExamRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamSeat;
import hr.fer.zemris.ferko.domain.model.Student;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Builds the "Moje provjere" view for the signed-in student: every assessment of the courses they
 * are enrolled in, with their personal room and seat once the schedule is published.
 */
public class StudentExamService {

  private final AppUserRepository userRepository;
  private final StudentRepository studentRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final CourseRepository courseRepository;
  private final ExamRepository examRepository;
  private final RoomRepository roomRepository;

  public StudentExamService(
      AppUserRepository userRepository,
      StudentRepository studentRepository,
      EnrollmentRepository enrollmentRepository,
      CourseRepository courseRepository,
      ExamRepository examRepository,
      RoomRepository roomRepository) {
    this.userRepository = userRepository;
    this.studentRepository = studentRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.courseRepository = courseRepository;
    this.examRepository = examRepository;
    this.roomRepository = roomRepository;
  }

  /**
   * The exams of every course the user is enrolled in. Returns an empty list when the user is not a
   * student.
   */
  public List<MyExamView> forStudent(String username) {
    Optional<AppUser> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      return List.of();
    }
    Optional<Student> student = studentRepository.findByUserId(user.get().id());
    if (student.isEmpty()) {
      return List.of();
    }
    long studentId = student.get().id();

    List<MyExamView> result = new ArrayList<>();
    for (Enrollment enrollment : enrollmentRepository.findByStudent(studentId)) {
      Course course = courseRepository.findById(enrollment.courseId()).orElse(null);
      if (course == null) {
        continue;
      }
      for (Exam exam : examRepository.findByCourse(course.id())) {
        result.add(toView(exam, course, studentId));
      }
    }
    result.sort(
        Comparator.comparing(MyExamView::startsAt, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparingLong(MyExamView::examId));
    return result;
  }

  private MyExamView toView(Exam exam, Course course, long studentId) {
    boolean registered =
        examRepository.findRegistrations(exam.id()).stream()
            .anyMatch(reg -> reg.studentId() == studentId);

    String roomCode = null;
    Integer seatNo = null;
    String testGroup = null;
    if (exam.published()) {
      ExamSeat seat =
          examRepository.findSeats(exam.id()).stream()
              .filter(s -> s.studentId() == studentId)
              .findFirst()
              .orElse(null);
      if (seat != null) {
        roomCode = roomRepository.findById(seat.roomId()).map(room -> room.code()).orElse(null);
        seatNo = seat.seatNo();
        testGroup = seat.testGroup();
      }
    }

    return new MyExamView(
        exam.id(),
        course.id(),
        course.code(),
        course.name(),
        exam.title(),
        exam.shortName(),
        exam.kind().name(),
        exam.startsAt(),
        exam.durationMinutes(),
        exam.maxPoints(),
        registered,
        exam.published(),
        roomCode,
        seatNo,
        testGroup);
  }
}
