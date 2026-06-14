package hr.fer.zemris.ferko.application.usecase.demonstrator;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.DemonstratorRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.application.usecase.demonstrator.DemonstratorViews.DemonstratorView;
import hr.fer.zemris.ferko.application.usecase.demonstrator.DemonstratorViews.MyDemonstratorDutyView;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Demonstrator;
import hr.fer.zemris.ferko.domain.model.Student;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages course demonstrators ("demonstrature") — students who assist a course's laboratory
 * exercises. Course holders assign and remove them; demonstrators see their own duties.
 */
public class DemonstratorService {

  private final DemonstratorRepository demonstratorRepository;
  private final StudentRepository studentRepository;
  private final AppUserRepository userRepository;
  private final CourseRepository courseRepository;

  public DemonstratorService(
      DemonstratorRepository demonstratorRepository,
      StudentRepository studentRepository,
      AppUserRepository userRepository,
      CourseRepository courseRepository) {
    this.demonstratorRepository = demonstratorRepository;
    this.studentRepository = studentRepository;
    this.userRepository = userRepository;
    this.courseRepository = courseRepository;
  }

  /** Demonstrators on a course, with student name and JMBAG. */
  public List<DemonstratorView> listForCourse(long courseId) {
    List<DemonstratorView> result = new ArrayList<>();
    for (Demonstrator demonstrator : demonstratorRepository.findByCourse(courseId)) {
      Student student = studentRepository.findById(demonstrator.studentId()).orElse(null);
      if (student == null) {
        continue;
      }
      String fullName =
          userRepository.findById(student.userId()).map(AppUser::fullName).orElse(student.jmbag());
      result.add(new DemonstratorView(student.id(), student.jmbag(), fullName));
    }
    return result;
  }

  /** Assigns the student with the given JMBAG as a demonstrator. False if no such student. */
  public boolean assignByJmbag(long courseId, String jmbag) {
    return studentRepository
        .findByJmbag(jmbag)
        .map(
            student -> {
              assign(courseId, student.id());
              return true;
            })
        .orElse(false);
  }

  /** Idempotently assigns a demonstrator by student id. */
  public void assign(long courseId, long studentId) {
    if (!demonstratorRepository.exists(courseId, studentId)) {
      demonstratorRepository.save(new Demonstrator(0L, courseId, studentId));
    }
  }

  /** Removes a demonstrator; returns false if the assignment did not exist. */
  public boolean remove(long courseId, long studentId) {
    return demonstratorRepository.delete(courseId, studentId);
  }

  /** Courses on which the signed-in user is a demonstrator. */
  public List<MyDemonstratorDutyView> myDuties(String username) {
    Optional<AppUser> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      return List.of();
    }
    Optional<Student> student = studentRepository.findByUserId(user.get().id());
    if (student.isEmpty()) {
      return List.of();
    }
    List<MyDemonstratorDutyView> result = new ArrayList<>();
    for (Demonstrator demonstrator : demonstratorRepository.findByStudent(student.get().id())) {
      Course course = courseRepository.findById(demonstrator.courseId()).orElse(null);
      if (course != null) {
        result.add(new MyDemonstratorDutyView(course.id(), course.code(), course.name()));
      }
    }
    return result;
  }
}
