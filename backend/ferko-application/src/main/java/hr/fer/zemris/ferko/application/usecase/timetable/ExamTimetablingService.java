package hr.fer.zemris.ferko.application.usecase.timetable;

import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.application.usecase.timetable.ExamTimetablingViews.ExamSlotAssignmentView;
import hr.fer.zemris.ferko.application.usecase.timetable.ExamTimetablingViews.GeneratedExamTimetableView;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.scheduling.ExamTimetableProblem;
import hr.fer.zemris.ferko.scheduling.OptimizationResult;
import hr.fer.zemris.ferko.scheduling.Optimizer;
import hr.fer.zemris.ferko.scheduling.Optimizers;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates a conflict-minimising exam timetable with the evolutionary engine (Čupić's exam-
 * timetable objective): each course's exam is placed in a slot so that no student sits two exams at
 * once, weighted by the number of shared students. Reports convergence and compares the generated
 * schedule against the historical (legacy) one on the same cohort.
 */
public class ExamTimetablingService {

  private static final long DEFAULT_SEED = 42L;
  private static final int MAX_SLOTS = 40;
  // Illustrative "day 0" of the Summer 2026 exam period; slot index maps to consecutive days from
  // here purely for display of the generated schedule.
  private static final LocalDate EXAM_PERIOD_START = LocalDate.of(2026, 6, 15);

  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final StudentRepository studentRepository;

  public ExamTimetablingService(
      CourseRepository courseRepository,
      EnrollmentRepository enrollmentRepository,
      StudentRepository studentRepository) {
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.studentRepository = studentRepository;
  }

  /** Course ids taken by at least one student in the given study year. */
  public List<Long> coursesForStudyYear(int studyYear) {
    Map<Long, Integer> yearByStudent =
        studentRepository.findAll().stream()
            .collect(Collectors.toMap(s -> s.id(), s -> s.yearOfStudy(), (a, b) -> a));
    Set<Long> courseIds = new LinkedHashSet<>();
    for (Course course : courseRepository.findAll()) {
      for (Enrollment enrollment : enrollmentRepository.findByCourse(course.id())) {
        if (yearByStudent.getOrDefault(enrollment.studentId(), -1) == studyYear) {
          courseIds.add(course.id());
          break;
        }
      }
    }
    return new ArrayList<>(courseIds);
  }

  /**
   * Generates an exam timetable for the given courses across {@code slots} exam periods. {@code
   * legacyAssignment} (courseId -> historical slot id) is optional; when present its weighted
   * conflict count is reported for comparison.
   */
  public GeneratedExamTimetableView generate(
      List<Long> courseIds, int slots, String algorithm, Map<Long, Integer> legacyAssignment) {
    List<Course> courses =
        courseIds.stream()
            .map(id -> courseRepository.findById(id).orElse(null))
            .filter(c -> c != null)
            .toList();
    int n = courses.size();
    int slotCount = Math.min(MAX_SLOTS, Math.max(1, slots));

    List<Set<Long>> students = new ArrayList<>(n);
    for (Course course : courses) {
      students.add(
          enrollmentRepository.findByCourse(course.id()).stream()
              .map(Enrollment::studentId)
              .collect(Collectors.toCollection(HashSet::new)));
    }

    int[][] shared = new int[n][n];
    long baseline = 0;
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        int count = intersectionSize(students.get(i), students.get(j));
        shared[i][j] = count;
        shared[j][i] = count;
        baseline += count;
      }
    }

    long legacyConflicts = legacyConflicts(courses, shared, legacyAssignment);

    if (n == 0) {
      return new GeneratedExamTimetableView(
          resolveName(algorithm),
          slotCount,
          0,
          0,
          0,
          legacyConflicts,
          true,
          0,
          List.of(),
          List.of());
    }

    ExamTimetableProblem problem = new ExamTimetableProblem(n, slotCount, shared);
    Optimizer optimizer = Optimizers.createDefault(resolveName(algorithm), DEFAULT_SEED);
    OptimizationResult result = optimizer.optimize(problem);

    List<ExamSlotAssignmentView> assignments = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      int slot = result.assignment()[i];
      assignments.add(
          new ExamSlotAssignmentView(
              courses.get(i).id(),
              courses.get(i).code(),
              courses.get(i).name(),
              slot,
              EXAM_PERIOD_START.plusDays(slot).toString()));
    }

    return new GeneratedExamTimetableView(
        result.algorithm(),
        slotCount,
        n,
        baseline,
        Math.round(result.penalty()),
        legacyConflicts,
        result.isPerfect(),
        result.iterations(),
        result.penaltyHistory(),
        assignments);
  }

  /** Weighted conflicts of an external (e.g. legacy) assignment; -1 when none supplied. */
  private static long legacyConflicts(
      List<Course> courses, int[][] shared, Map<Long, Integer> legacyAssignment) {
    if (legacyAssignment == null || legacyAssignment.isEmpty()) {
      return -1;
    }
    int n = courses.size();
    int[] slot = new int[n];
    int unique = -1;
    for (int i = 0; i < n; i++) {
      Integer assigned = legacyAssignment.get(courses.get(i).id());
      slot[i] = assigned != null ? assigned : unique--;
    }
    long conflicts = 0;
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        if (slot[i] == slot[j]) {
          conflicts += shared[i][j];
        }
      }
    }
    return conflicts;
  }

  private static int intersectionSize(Set<Long> a, Set<Long> b) {
    Set<Long> smaller = a.size() <= b.size() ? a : b;
    Set<Long> larger = smaller == a ? b : a;
    int count = 0;
    for (Long id : smaller) {
      if (larger.contains(id)) {
        count++;
      }
    }
    return count;
  }

  private static String resolveName(String algorithm) {
    if (algorithm == null || algorithm.isBlank()) {
      return "GENETIC";
    }
    String normalized = algorithm.trim().toUpperCase(Locale.ROOT);
    return Optimizers.names().contains(normalized) ? normalized : "GENETIC";
  }
}
