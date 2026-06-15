package hr.fer.zemris.ferko.application.usecase.timetable;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.CourseRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingViews.AlgorithmComparisonView;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingViews.AppliedTimetableView;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingViews.ComparisonView;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingViews.CourseAssignmentView;
import hr.fer.zemris.ferko.application.usecase.timetable.LectureTimetablingViews.GeneratedTimetableView;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.GroupType;
import hr.fer.zemris.ferko.scheduling.OptimizationResult;
import hr.fer.zemris.ferko.scheduling.Optimizer;
import hr.fer.zemris.ferko.scheduling.Optimizers;
import hr.fer.zemris.ferko.scheduling.SimpleSchedulingProblem;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates a conflict-minimising weekly lecture timetable with the evolutionary engine: each
 * course in scope is assigned one weekly period so that courses sharing students collide as little
 * as possible (Čupić's simple-scheduling model). Runs on demand for a chosen scope (a study year or
 * an explicit course set) and reports the convergence and the conflict reduction versus the
 * all-in-one-slot baseline.
 */
public class LectureTimetablingService {

  private static final long DEFAULT_SEED = 42L;
  private static final int DAYS_PER_WEEK = 5;
  private static final LocalTime FIRST_SLOT = LocalTime.of(8, 0);
  private static final int SLOT_HOURS = 2;
  // 6 two-hour blocks per day (08:00–20:00) x 5 working days bounds the weekly grid so generated
  // start times never wrap past the working day.
  private static final int MAX_BLOCKS_PER_DAY = 6;
  private static final int MAX_PERIODS = DAYS_PER_WEEK * MAX_BLOCKS_PER_DAY;

  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final StudentRepository studentRepository;
  private final ClassScheduleRepository classScheduleRepository;

  public LectureTimetablingService(
      CourseRepository courseRepository,
      EnrollmentRepository enrollmentRepository,
      StudentRepository studentRepository,
      ClassScheduleRepository classScheduleRepository) {
    this.courseRepository = courseRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.studentRepository = studentRepository;
    this.classScheduleRepository = classScheduleRepository;
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
   * Generates a timetable for the given courses across {@code periods} weekly slots using the named
   * optimizer ({@link Optimizers#names()}); a blank algorithm uses the default genetic algorithm.
   */
  public GeneratedTimetableView generate(List<Long> courseIds, int periods, String algorithm) {
    int slots = Math.min(MAX_PERIODS, Math.max(1, periods));
    Scope scope = buildScope(courseIds);
    int n = scope.courses().size();

    if (n == 0) {
      return new GeneratedTimetableView(
          resolveName(algorithm), slots, 0, 0, 0, true, 0, List.of(), List.of());
    }

    SimpleSchedulingProblem problem = new SimpleSchedulingProblem(slots, scope.conflict());
    Optimizer optimizer = resolveOptimizer(algorithm);
    OptimizationResult result = optimizer.optimize(problem);

    List<CourseAssignmentView> assignments = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      int period = result.assignment()[i];
      assignments.add(
          new CourseAssignmentView(
              scope.courses().get(i).id(),
              scope.courses().get(i).code(),
              scope.courses().get(i).name(),
              period,
              dayOf(period).name(),
              startOf(period).toString()));
    }

    return new GeneratedTimetableView(
        result.algorithm(),
        slots,
        n,
        scope.baseline(),
        (int) Math.round(result.penalty()),
        result.isPerfect(),
        result.iterations(),
        result.penaltyHistory(),
        assignments);
  }

  /**
   * Runs every metaheuristic on the same lecture-timetabling problem and returns each one's result
   * and convergence, sorted best (fewest conflicts) first — a fair comparison of the engine's
   * algorithm families on identical input.
   */
  public ComparisonView compare(List<Long> courseIds, int periods) {
    int slots = Math.min(MAX_PERIODS, Math.max(1, periods));
    Scope scope = buildScope(courseIds);
    int n = scope.courses().size();

    List<AlgorithmComparisonView> runs = new ArrayList<>();
    if (n > 0) {
      SimpleSchedulingProblem problem = new SimpleSchedulingProblem(slots, scope.conflict());
      for (String name : Optimizers.names()) {
        Optimizer optimizer = Optimizers.createDefault(name, DEFAULT_SEED);
        long start = System.nanoTime();
        OptimizationResult result = optimizer.optimize(problem);
        long millis = (System.nanoTime() - start) / 1_000_000L;
        runs.add(
            new AlgorithmComparisonView(
                name,
                (int) Math.round(result.penalty()),
                result.iterations(),
                result.isPerfect(),
                millis,
                result.penaltyHistory()));
      }
      runs.sort((a, b) -> Integer.compare(a.conflicts(), b.conflicts()));
    }
    return new ComparisonView(n, slots, scope.baseline(), runs);
  }

  /**
   * Generates a timetable and persists it: each course's existing slots are replaced by its
   * generated weekly lecture slot. This makes the engine's proposal the course's actual timetable.
   */
  public AppliedTimetableView apply(List<Long> courseIds, int periods, String algorithm) {
    int slots = Math.min(MAX_PERIODS, Math.max(1, periods));
    Scope scope = buildScope(courseIds);
    int n = scope.courses().size();
    if (n == 0) {
      return new AppliedTimetableView(resolveName(algorithm), 0, 0, 0, 0, true);
    }

    SimpleSchedulingProblem problem = new SimpleSchedulingProblem(slots, scope.conflict());
    OptimizationResult result = resolveOptimizer(algorithm).optimize(problem);

    int written = 0;
    for (int i = 0; i < n; i++) {
      long courseId = scope.courses().get(i).id();
      int period = result.assignment()[i];
      classScheduleRepository.deleteByCourse(courseId);
      classScheduleRepository.save(
          new ClassSchedule(
              0L,
              courseId,
              null,
              GroupType.LECTURE,
              null,
              dayOf(period),
              startOf(period),
              startOf(period).plusHours(SLOT_HOURS),
              ""));
      written++;
    }

    return new AppliedTimetableView(
        resolveName(algorithm),
        n,
        written,
        scope.baseline(),
        (int) Math.round(result.penalty()),
        result.isPerfect());
  }

  /** Resolved courses plus their student-sharing conflict matrix and all-in-one-slot baseline. */
  private record Scope(List<Course> courses, boolean[][] conflict, int baseline) {}

  /** Builds the conflict matrix for a course set (pair conflicts when courses share a student). */
  private Scope buildScope(List<Long> courseIds) {
    List<Course> courses =
        courseIds.stream()
            .map(id -> courseRepository.findById(id).orElse(null))
            .filter(c -> c != null)
            .toList();
    int n = courses.size();
    List<Set<Long>> students = new ArrayList<>(n);
    for (Course course : courses) {
      students.add(
          enrollmentRepository.findByCourse(course.id()).stream()
              .map(Enrollment::studentId)
              .collect(Collectors.toCollection(HashSet::new)));
    }
    boolean[][] conflict = new boolean[n][n];
    int baseline = 0;
    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        if (shareStudent(students.get(i), students.get(j))) {
          conflict[i][j] = true;
          conflict[j][i] = true;
          baseline++;
        }
      }
    }
    return new Scope(courses, conflict, baseline);
  }

  private Optimizer resolveOptimizer(String algorithm) {
    return Optimizers.createDefault(resolveName(algorithm), DEFAULT_SEED);
  }

  /** Normalises the requested algorithm name, falling back to GENETIC if blank or unknown. */
  private static String resolveName(String algorithm) {
    if (algorithm == null || algorithm.isBlank()) {
      return "GENETIC";
    }
    String normalized = algorithm.trim().toUpperCase(Locale.ROOT);
    return Optimizers.selectable().contains(normalized) ? normalized : "GENETIC";
  }

  private static boolean shareStudent(Set<Long> a, Set<Long> b) {
    Set<Long> smaller = a.size() <= b.size() ? a : b;
    Set<Long> larger = smaller == a ? b : a;
    for (Long id : smaller) {
      if (larger.contains(id)) {
        return true;
      }
    }
    return false;
  }

  private static DayOfWeek dayOf(int period) {
    return DayOfWeek.of((period % DAYS_PER_WEEK) + 1);
  }

  private static LocalTime startOf(int period) {
    return FIRST_SLOT.plusHours((long) SLOT_HOURS * (period / DAYS_PER_WEEK));
  }
}
