package hr.fer.zemris.ferko.application.usecase.grading;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.GradingRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.Grade;
import hr.fer.zemris.ferko.domain.model.GradeComponent;
import hr.fer.zemris.ferko.domain.model.Student;
import hr.fer.zemris.ferko.domain.model.StudentPoints;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Grade components, points entry, the "preglednik bodova" overview and final grades. */
public class GradingService {

  private final GradingRepository gradingRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final StudentRepository studentRepository;
  private final AppUserRepository userRepository;

  public GradingService(
      GradingRepository gradingRepository,
      EnrollmentRepository enrollmentRepository,
      StudentRepository studentRepository,
      AppUserRepository userRepository) {
    this.gradingRepository = gradingRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.studentRepository = studentRepository;
    this.userRepository = userRepository;
  }

  public GradeComponentView addComponent(
      long courseId, String name, String shortName, double maxPoints, int ordinal) {
    GradeComponent saved =
        gradingRepository.addComponent(
            new GradeComponent(0L, courseId, name, shortName, maxPoints, ordinal));
    return toView(saved);
  }

  public List<GradeComponentView> listComponents(long courseId) {
    return gradingRepository.componentsByCourse(courseId).stream()
        .map(GradingService::toView)
        .toList();
  }

  public void enterPoints(
      long courseId, long studentId, long componentId, double points, String enteredBy) {
    double maxPoints =
        gradingRepository.componentsByCourse(courseId).stream()
            .filter(component -> component.id() == componentId)
            .map(GradeComponent::maxPoints)
            .findFirst()
            .orElse(0.0);
    gradingRepository.savePoints(
        new StudentPoints(
            0L,
            courseId,
            studentId,
            componentId,
            null,
            points,
            maxPoints,
            true,
            enteredBy,
            LocalDateTime.now()));
  }

  public List<PointsOverviewRow> pointsOverview(long courseId) {
    List<GradeComponent> components = gradingRepository.componentsByCourse(courseId);
    Map<Long, String> componentName =
        components.stream()
            .collect(Collectors.toMap(GradeComponent::id, GradeComponent::shortName));

    Map<Long, Map<String, Double>> pointsByStudent = new LinkedHashMap<>();
    for (StudentPoints points : gradingRepository.pointsByCourse(courseId)) {
      if (points.componentId() == null) {
        continue;
      }
      String shortName = componentName.get(points.componentId());
      if (shortName != null) {
        pointsByStudent
            .computeIfAbsent(points.studentId(), key -> new LinkedHashMap<>())
            .merge(shortName, points.points(), Double::sum);
      }
    }

    Map<Long, Integer> gradeByStudent =
        gradingRepository.gradesByCourse(courseId).stream()
            .collect(Collectors.toMap(Grade::studentId, Grade::finalGrade));

    Map<Long, Student> studentsById = new LinkedHashMap<>();
    studentRepository.findAll().forEach(student -> studentsById.put(student.id(), student));
    Map<Long, String> userNames =
        userRepository.findAll().stream()
            .collect(Collectors.toMap(AppUser::id, AppUser::fullName, (a, b) -> a));

    List<PointsOverviewRow> rows = new ArrayList<>();
    for (Enrollment enrollment : enrollmentRepository.findByCourse(courseId)) {
      Student student = studentsById.get(enrollment.studentId());
      if (student == null) {
        continue;
      }
      Map<String, Double> points = pointsByStudent.getOrDefault(student.id(), Map.of());
      double total = points.values().stream().mapToDouble(Double::doubleValue).sum();
      rows.add(
          new PointsOverviewRow(
              student.id(),
              student.jmbag(),
              userNames.getOrDefault(student.userId(), ""),
              points,
              total,
              gradeByStudent.getOrDefault(student.id(), 0)));
    }
    rows.sort((a, b) -> a.jmbag().compareTo(b.jmbag()));
    return rows;
  }

  public void assignGrade(long courseId, long studentId, int finalGrade, String decidedBy) {
    double total =
        gradingRepository.pointsByCourse(courseId).stream()
            .filter(points -> points.studentId() == studentId && points.componentId() != null)
            .mapToDouble(StudentPoints::points)
            .sum();
    recordGrade(courseId, studentId, finalGrade, total, decidedBy);
  }

  /**
   * Records a final grade with an already-computed total, without re-summing the student's points.
   * Used by bulk seeding where the total is known.
   */
  public void recordGrade(
      long courseId, long studentId, int finalGrade, double total, String decidedBy) {
    gradingRepository.saveGrade(
        new Grade(0L, courseId, studentId, finalGrade, total, decidedBy, LocalDateTime.now()));
  }

  public List<GradeView> listGrades(long courseId) {
    Map<Long, Student> studentsById = new LinkedHashMap<>();
    studentRepository.findAll().forEach(student -> studentsById.put(student.id(), student));
    Map<Long, String> userNames =
        userRepository.findAll().stream()
            .collect(Collectors.toMap(AppUser::id, AppUser::fullName, (a, b) -> a));
    return gradingRepository.gradesByCourse(courseId).stream()
        .map(
            grade -> {
              Student student = studentsById.get(grade.studentId());
              String jmbag = student == null ? "" : student.jmbag();
              String fullName = student == null ? "" : userNames.getOrDefault(student.userId(), "");
              return new GradeView(
                  grade.studentId(), jmbag, fullName, grade.finalGrade(), grade.pointsTotal());
            })
        .toList();
  }

  private static GradeComponentView toView(GradeComponent component) {
    return new GradeComponentView(
        component.id(),
        component.name(),
        component.shortName(),
        component.maxPoints(),
        component.ordinal());
  }
}
