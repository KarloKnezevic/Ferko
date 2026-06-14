package hr.fer.zemris.ferko.application.usecase.exam;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.ExamRepository;
import hr.fer.zemris.ferko.application.port.MailSender;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.application.port.StudentRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamKind;
import hr.fer.zemris.ferko.domain.model.ExamRegistration;
import hr.fer.zemris.ferko.domain.model.ExamRoom;
import hr.fer.zemris.ferko.domain.model.ExamSeat;
import hr.fer.zemris.ferko.domain.model.ExamVisibility;
import hr.fer.zemris.ferko.domain.model.Room;
import hr.fer.zemris.ferko.domain.model.Student;
import hr.fer.zemris.ferko.scheduling.GaConfig;
import hr.fer.zemris.ferko.scheduling.GeneticAlgorithm;
import hr.fer.zemris.ferko.scheduling.OptimizationResult;
import hr.fer.zemris.ferko.scheduling.Optimizer;
import hr.fer.zemris.ferko.scheduling.Optimizers;
import hr.fer.zemris.ferko.scheduling.SeatingProblem;
import hr.fer.zemris.ferko.scheduling.SeatingStrategies;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orchestrates the assessment workflow: defining exams, reserving rooms, registering students and
 * producing the seating with either a deterministic FERKO strategy or the genetic optimiser from
 * {@code ferko-scheduling}.
 */
public class ExamSchedulingService {

  private static final double OVER_CAPACITY_ALPHA = 2.0;
  private static final long DEFAULT_SEED = 42L;
  private static final int COMPARE_POPULATION = 40;
  private static final int COMPARE_ITERATIONS = 800;

  private final ExamRepository examRepository;
  private final RoomRepository roomRepository;
  private final StudentRepository studentRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final AppUserRepository userRepository;
  private final MailSender mailSender;

  public ExamSchedulingService(
      ExamRepository examRepository,
      RoomRepository roomRepository,
      StudentRepository studentRepository,
      EnrollmentRepository enrollmentRepository,
      AppUserRepository userRepository,
      MailSender mailSender) {
    this.examRepository = examRepository;
    this.roomRepository = roomRepository;
    this.studentRepository = studentRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.userRepository = userRepository;
    this.mailSender = mailSender;
  }

  public long createExam(
      long courseId,
      String title,
      String shortName,
      String kind,
      LocalDateTime startsAt,
      int durationMinutes,
      double maxPoints) {
    Exam exam =
        examRepository.save(
            new Exam(
                0L,
                courseId,
                title,
                shortName,
                ExamKind.valueOf(kind),
                startsAt,
                durationMinutes,
                maxPoints,
                0,
                ExamVisibility.ALWAYS,
                false,
                null,
                false));
    return exam.id();
  }

  public void reserveRoom(long examId, long roomId, int capacity, int requiredAssistants) {
    examRepository.addRoom(new ExamRoom(0L, examId, roomId, capacity, requiredAssistants, true));
  }

  public int registerEnrolledStudents(long examId, long courseId) {
    List<Long> already =
        examRepository.findRegistrations(examId).stream().map(ExamRegistration::studentId).toList();
    int added = 0;
    for (var enrollment : enrollmentRepository.findByCourse(courseId)) {
      if (!already.contains(enrollment.studentId())) {
        examRepository.addRegistration(
            new ExamRegistration(
                0L, examId, enrollment.studentId(), LocalDateTime.now(), "REGISTERED"));
        added++;
      }
    }
    return added;
  }

  /**
   * Registers the signed-in student for an exam ("prijava na provjeru"). Returns {@code false} when
   * the exam or student does not exist, or the student is not enrolled in the exam's course;
   * idempotent when already registered. Throws {@link IllegalStateException} once the schedule is
   * published (registration window closed).
   */
  public boolean registerSelf(long examId, String username) {
    Exam exam = examRepository.findById(examId).orElse(null);
    if (exam == null) {
      return false;
    }
    if (exam.published()) {
      throw new IllegalStateException("Prijava je zatvorena — raspored je objavljen.");
    }
    Long studentId = enrolledStudentId(username, exam.courseId());
    if (studentId == null) {
      return false;
    }
    boolean already =
        examRepository.findRegistrations(examId).stream()
            .anyMatch(reg -> reg.studentId() == studentId);
    if (!already) {
      examRepository.addRegistration(
          new ExamRegistration(0L, examId, studentId, LocalDateTime.now(), "REGISTERED"));
    }
    return true;
  }

  /**
   * Cancels the signed-in student's registration for an exam ("odjava"). Returns {@code false} when
   * the exam or student does not exist; throws {@link IllegalStateException} once the schedule is
   * published.
   */
  public boolean unregisterSelf(long examId, String username) {
    Exam exam = examRepository.findById(examId).orElse(null);
    if (exam == null) {
      return false;
    }
    if (exam.published()) {
      throw new IllegalStateException("Odjava je zatvorena — raspored je objavljen.");
    }
    Student student =
        userRepository
            .findByUsername(username)
            .flatMap(user -> studentRepository.findByUserId(user.id()))
            .orElse(null);
    if (student == null) {
      return false;
    }
    examRepository.removeRegistration(examId, student.id());
    return true;
  }

  private Long enrolledStudentId(String username, long courseId) {
    Student student =
        userRepository
            .findByUsername(username)
            .flatMap(user -> studentRepository.findByUserId(user.id()))
            .orElse(null);
    if (student == null) {
      return null;
    }
    return enrollmentRepository.findByStudentAndCourse(student.id(), courseId).isPresent()
        ? student.id()
        : null;
  }

  public List<ExamView> listExams(long courseId) {
    return examRepository.findByCourse(courseId).stream().map(this::toExamView).toList();
  }

  public SeatingResult generateSeating(long examId, SeatingStrategy strategy) {
    List<Long> studentIds =
        examRepository.findRegistrations(examId).stream().map(ExamRegistration::studentId).toList();
    List<ExamRoom> rooms = examRepository.findRooms(examId);
    if (rooms.isEmpty()) {
      throw new IllegalStateException("Provjera nema rezerviranih dvorana.");
    }

    int[] capacities = rooms.stream().mapToInt(ExamRoom::capacity).toArray();
    int studentCount = studentIds.size();

    int[] assignment;
    List<Double> history = List.of();
    if (strategy == SeatingStrategy.GENETIC) {
      SeatingProblem problem = new SeatingProblem(studentCount, capacities, OVER_CAPACITY_ALPHA);
      OptimizationResult result = new GeneticAlgorithm(GaConfig.defaults()).optimize(problem);
      assignment = result.assignment();
      history = result.penaltyHistory();
    } else {
      assignment = deterministic(strategy, studentCount, capacities);
    }

    List<ExamSeat> seats = buildSeats(examId, studentIds, rooms, assignment);
    examRepository.replaceSeats(examId, seats);

    double penalty =
        new SeatingProblem(studentCount, capacities, OVER_CAPACITY_ALPHA).penalty(assignment);
    return new SeatingResult(
        strategy.name(), seats.size(), penalty, penalty <= 0.0, history, roomSeating(examId));
  }

  public List<RoomSeatingView> roomSeating(long examId) {
    Map<Long, String> userNames = userNames();
    Map<Long, Student> studentsById = new LinkedHashMap<>();
    for (Student student : studentRepository.findAll()) {
      studentsById.put(student.id(), student);
    }

    List<ExamSeat> seats = examRepository.findSeats(examId);
    Map<Long, List<ExamSeatView>> byRoom = new LinkedHashMap<>();
    for (ExamSeat seat : seats) {
      Student student = studentsById.get(seat.studentId());
      String jmbag = student == null ? "" : student.jmbag();
      String fullName = student == null ? "" : userNames.getOrDefault(student.userId(), "");
      Room room = roomRepository.findById(seat.roomId()).orElse(null);
      String roomCode = room == null ? "" : room.code();
      byRoom
          .computeIfAbsent(seat.roomId(), key -> new ArrayList<>())
          .add(
              new ExamSeatView(
                  seat.studentId(),
                  jmbag,
                  fullName,
                  seat.roomId(),
                  roomCode,
                  seat.seatNo(),
                  seat.testGroup()));
    }

    List<RoomSeatingView> result = new ArrayList<>();
    for (ExamRoom examRoom : examRepository.findRooms(examId)) {
      List<ExamSeatView> roomSeats = byRoom.getOrDefault(examRoom.roomId(), List.of());
      String roomCode = roomRepository.findById(examRoom.roomId()).map(Room::code).orElse("");
      result.add(
          new RoomSeatingView(
              examRoom.roomId(), roomCode, examRoom.capacity(), roomSeats.size(), roomSeats));
    }
    return result;
  }

  /**
   * Runs an arbitrary named metaheuristic (any of {@link Optimizers#names()}) over the exam's
   * seating problem and persists the resulting seating, returning the run summary.
   */
  public SeatingResult generateSeatingWith(long examId, String algorithm) {
    SeatingSetup setup = seatingSetup(examId);
    int studentCount = setup.studentIds().size();
    SeatingProblem problem =
        new SeatingProblem(studentCount, setup.capacities(), OVER_CAPACITY_ALPHA);
    Optimizer optimizer = Optimizers.createDefault(algorithm, DEFAULT_SEED);
    OptimizationResult result = optimizer.optimize(problem);

    List<ExamSeat> seats =
        buildSeats(examId, setup.studentIds(), setup.rooms(), result.assignment());
    examRepository.replaceSeats(examId, seats);

    double penalty = problem.penalty(result.assignment());
    return new SeatingResult(
        algorithm,
        seats.size(),
        penalty,
        penalty <= 0.0,
        result.penaltyHistory(),
        roomSeating(examId));
  }

  /**
   * Runs every supported metaheuristic over the exam's seating problem with an identical budget and
   * seed and returns their results (penalty + convergence curve), sorted best-first, without
   * persisting any seating — the FERKO "izbor algoritma + usporedni prikaz".
   */
  public List<AlgorithmRunView> compareSeatingAlgorithms(long examId) {
    SeatingSetup setup = seatingSetup(examId);
    int studentCount = setup.studentIds().size();
    List<AlgorithmRunView> runs = new ArrayList<>();
    for (String name : Optimizers.names()) {
      SeatingProblem problem =
          new SeatingProblem(studentCount, setup.capacities(), OVER_CAPACITY_ALPHA);
      Optimizer optimizer =
          Optimizers.create(name, COMPARE_POPULATION, COMPARE_ITERATIONS, DEFAULT_SEED);
      long start = System.nanoTime();
      OptimizationResult result = optimizer.optimize(problem);
      long millis = (System.nanoTime() - start) / 1_000_000L;
      runs.add(
          new AlgorithmRunView(
              name,
              result.penalty(),
              result.iterations(),
              result.penalty() <= 0.0,
              millis,
              result.penaltyHistory()));
    }
    runs.sort(Comparator.comparingDouble(AlgorithmRunView::penalty));
    return runs;
  }

  public void publish(long examId) {
    examRepository.markPublished(examId, true);
    Exam exam = examRepository.findById(examId).orElse(null);
    if (exam == null) {
      return;
    }
    List<String> recipients = registeredStudentEmails(examId);
    if (!recipients.isEmpty()) {
      mailSender.send(
          recipients,
          "Objavljen raspored provjere: " + exam.shortName(),
          "Raspored provjere \""
              + exam.title()
              + "\" je objavljen. Provjerite svoju dvoranu i mjesto u sustavu FERKO.");
    }
  }

  private List<String> registeredStudentEmails(long examId) {
    Map<Long, Student> studentsById = new LinkedHashMap<>();
    for (Student student : studentRepository.findAll()) {
      studentsById.put(student.id(), student);
    }
    Map<Long, String> emailByUserId =
        userRepository.findAll().stream()
            .filter(user -> user.email() != null && !user.email().isBlank())
            .collect(Collectors.toMap(AppUser::id, AppUser::email, (a, b) -> a));
    return examRepository.findRegistrations(examId).stream()
        .map(registration -> studentsById.get(registration.studentId()))
        .filter(student -> student != null)
        .map(student -> emailByUserId.get(student.userId()))
        .filter(email -> email != null && !email.isBlank())
        .distinct()
        .toList();
  }

  private SeatingSetup seatingSetup(long examId) {
    List<Long> studentIds =
        examRepository.findRegistrations(examId).stream().map(ExamRegistration::studentId).toList();
    List<ExamRoom> rooms = examRepository.findRooms(examId);
    if (rooms.isEmpty()) {
      throw new IllegalStateException("Provjera nema rezerviranih dvorana.");
    }
    int[] capacities = rooms.stream().mapToInt(ExamRoom::capacity).toArray();
    return new SeatingSetup(studentIds, rooms, capacities);
  }

  private record SeatingSetup(List<Long> studentIds, List<ExamRoom> rooms, int[] capacities) {}

  private int[] deterministic(SeatingStrategy strategy, int studentCount, int[] capacities) {
    return switch (strategy) {
      case SORTED_GREEDY -> SeatingStrategies.sortedGreedy(studentCount, capacities);
      case SORTED_PROPORTIONAL -> SeatingStrategies.sortedProportional(studentCount, capacities);
      case RANDOM_GREEDY -> SeatingStrategies.randomGreedy(studentCount, capacities, DEFAULT_SEED);
      case RANDOM_PROPORTIONAL ->
          SeatingStrategies.randomProportional(studentCount, capacities, DEFAULT_SEED);
      default -> throw new IllegalArgumentException("Unsupported strategy: " + strategy);
    };
  }

  private List<ExamSeat> buildSeats(
      long examId, List<Long> studentIds, List<ExamRoom> rooms, int[] assignment) {
    int[] seatCounter = new int[rooms.size()];
    List<ExamSeat> seats = new ArrayList<>();
    for (int i = 0; i < studentIds.size(); i++) {
      int roomIndex = assignment[i];
      long roomId = rooms.get(roomIndex).roomId();
      int seatNo = ++seatCounter[roomIndex];
      seats.add(new ExamSeat(0L, examId, studentIds.get(i), roomId, seatNo, null));
    }
    return seats;
  }

  private ExamView toExamView(Exam exam) {
    List<ExamRoom> rooms = examRepository.findRooms(exam.id());
    int totalCapacity = rooms.stream().mapToInt(ExamRoom::capacity).sum();
    return new ExamView(
        exam.id(),
        exam.courseId(),
        exam.title(),
        exam.shortName(),
        exam.kind().name(),
        exam.startsAt(),
        exam.durationMinutes(),
        exam.maxPoints(),
        exam.published(),
        examRepository.findRegistrations(exam.id()).size(),
        rooms.size(),
        totalCapacity,
        examRepository.findSeats(exam.id()).size());
  }

  private Map<Long, String> userNames() {
    return userRepository.findAll().stream()
        .collect(Collectors.toMap(AppUser::id, AppUser::fullName, (a, b) -> a));
  }
}
