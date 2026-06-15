package hr.fer.zemris.ferko.application.usecase.timetable;

import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.SemesterRepository;
import hr.fer.zemris.ferko.application.usecase.timetable.CourseConflictMatrixViews.CourseAxis;
import hr.fer.zemris.ferko.application.usecase.timetable.CourseConflictMatrixViews.CourseConflictMatrixView;
import hr.fer.zemris.ferko.application.usecase.timetable.CourseConflictMatrixViews.MatrixCell;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the course-overlap conflict matrix used to drive (and reason about) exam timetabling, as
 * described in M. Čupić's exam-timetabling work: each cell {@code (i, j)} holds the number of
 * students enrolled in <em>both</em> courses {@code i} and {@code j}. Two courses that share
 * students cannot have their exams in the same timeslot (a hard constraint), and the more students
 * they share the further apart in time their exams should be (a soft constraint) — so this matrix
 * is the conflict density map a scheduler (human or evolutionary) works against. A higher cell
 * value means more shared students (rendered "hotter"); a zero means the two courses share no
 * students.
 */
public class CourseConflictMatrixService {

  private final SemesterRepository semesterRepository;
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;

  public CourseConflictMatrixService(
      SemesterRepository semesterRepository,
      CourseRepository courseRepository,
      EnrollmentRepository enrollmentRepository) {
    this.semesterRepository = semesterRepository;
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
  }

  /**
   * The symmetric course-overlap matrix for the given semester (or the active semester when {@code
   * semesterCode} is blank). Cells are returned sparsely: only the upper triangle ({@code i < j})
   * with at least one shared student is included; the diagonal is each course's own enrolment.
   */
  public CourseConflictMatrixView matrix(String semesterCode) {
    String code = resolveSemester(semesterCode);
    List<Course> courses =
        new ArrayList<>(
            code == null ? courseRepository.findAll() : courseRepository.findBySemester(code));
    courses.sort(Comparator.comparing(Course::code).thenComparingLong(Course::id));

    int n = courses.size();
    List<CourseAxis> axis = new ArrayList<>(n);
    // Map each student to the indices of the (matrix) courses they are enrolled in. Inverting the
    // enrolment this way keeps the pair counting at O(sum of pairs-per-student), far below O(n^2).
    Map<Long, List<Integer>> coursesByStudent = new HashMap<>();
    for (int i = 0; i < n; i++) {
      Course course = courses.get(i);
      List<Enrollment> enrollments = enrollmentRepository.findByCourse(course.id());
      axis.add(new CourseAxis(course.id(), course.code(), course.name(), enrollments.size()));
      for (Enrollment enrollment : enrollments) {
        coursesByStudent.computeIfAbsent(enrollment.studentId(), key -> new ArrayList<>()).add(i);
      }
    }

    Map<Long, Integer> shared = new HashMap<>();
    for (List<Integer> indices : coursesByStudent.values()) {
      indices.sort(Comparator.naturalOrder());
      for (int a = 0; a < indices.size(); a++) {
        for (int b = a + 1; b < indices.size(); b++) {
          long key = (long) indices.get(a) * n + indices.get(b);
          shared.merge(key, 1, Integer::sum);
        }
      }
    }

    List<MatrixCell> cells = new ArrayList<>(shared.size());
    int max = 0;
    for (Map.Entry<Long, Integer> entry : shared.entrySet()) {
      int i = (int) (entry.getKey() / n);
      int j = (int) (entry.getKey() % n);
      int value = entry.getValue();
      cells.add(new MatrixCell(i, j, value));
      max = Math.max(max, value);
    }
    cells.sort(Comparator.comparingInt(MatrixCell::i).thenComparingInt(MatrixCell::j));
    return new CourseConflictMatrixView(code, axis, cells, max);
  }

  private String resolveSemester(String semesterCode) {
    if (semesterCode != null && !semesterCode.isBlank()) {
      return semesterCode;
    }
    return semesterRepository.findActive().map(s -> s.code()).orElse(null);
  }
}
