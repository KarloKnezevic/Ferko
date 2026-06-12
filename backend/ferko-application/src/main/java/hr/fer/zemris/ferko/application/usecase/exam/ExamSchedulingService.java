package hr.fer.zemris.ferko.application.usecase.exam;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.EnrollmentRepository;
import hr.fer.zemris.ferko.application.port.ExamRepository;
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
import hr.fer.zemris.ferko.scheduling.GaResult;
import hr.fer.zemris.ferko.scheduling.GeneticAlgorithm;
import hr.fer.zemris.ferko.scheduling.SeatingProblem;
import hr.fer.zemris.ferko.scheduling.SeatingStrategies;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

  private final ExamRepository examRepository;
  private final RoomRepository roomRepository;
  private final StudentRepository studentRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final AppUserRepository userRepository;

  public ExamSchedulingService(
      ExamRepository examRepository,
      RoomRepository roomRepository,
      StudentRepository studentRepository,
      EnrollmentRepository enrollmentRepository,
      AppUserRepository userRepository) {
    this.examRepository = examRepository;
    this.roomRepository = roomRepository;
    this.studentRepository = studentRepository;
    this.enrollmentRepository = enrollmentRepository;
    this.userRepository = userRepository;
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
      GaResult result = new GeneticAlgorithm(GaConfig.defaults()).solve(problem);
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

  public void publish(long examId) {
    examRepository.markPublished(examId, true);
  }

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
