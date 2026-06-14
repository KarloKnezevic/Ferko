package hr.fer.zemris.ferko.application.usecase.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.support.InMemoryExamRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Room;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.LocalDateTime;
import java.util.EnumSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExamSchedulingServiceTest {

  private static final long COURSE_ID = 100L;

  private InMemoryExamRepository exams;
  private InMemoryAcademicRepositories.Rooms rooms;
  private InMemoryAcademicRepositories.Students students;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private InMemoryAcademicRepositories.Users users;
  private RecordingMailSender mail;
  private ExamSchedulingService service;

  private long roomA;
  private long roomB;

  @BeforeEach
  void setUp() {
    exams = new InMemoryExamRepository();
    rooms = new InMemoryAcademicRepositories.Rooms();
    students = new InMemoryAcademicRepositories.Students();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    users = new InMemoryAcademicRepositories.Users();
    mail = new RecordingMailSender();
    service = new ExamSchedulingService(exams, rooms, students, enrollments, users, mail);

    roomA = rooms.save(new Room(0L, "A101", "A", 15, 2, false)).id();
    roomB = rooms.save(new Room(0L, "B1", "B", 15, 2, false)).id();

    for (int i = 0; i < 20; i++) {
      long userId =
          users
              .save(
                  new AppUser(
                      0L,
                      "stud" + i,
                      "h",
                      "Student " + i,
                      "s" + i + "@fer.hr",
                      true,
                      LocalDateTime.now(),
                      EnumSet.of(Role.STUDENT)))
              .id();
      long studentId = students.save(new Student(0L, userId, "00360" + i, "Računarstvo", 1)).id();
      enrollments.save(
          new Enrollment(0L, studentId, COURSE_ID, LocalDateTime.now(), EnrollmentStatus.ACTIVE));
    }
  }

  @Test
  void geneticSeatingPlacesEveryStudentWithinCapacity() {
    long examId =
        service.createExam(
            COURSE_ID, "Prvi međuispit", "MI1", "MEDJUISPIT", LocalDateTime.now(), 90, 20.0);
    service.reserveRoom(examId, roomA, 15, 2);
    service.reserveRoom(examId, roomB, 15, 2);

    int registered = service.registerEnrolledStudents(examId, COURSE_ID);
    assertEquals(20, registered);

    SeatingResult result = service.generateSeating(examId, SeatingStrategy.GENETIC);

    assertEquals(20, result.seatedStudents());
    assertTrue(
        result.feasible(),
        "expected a within-capacity seating, penalty=" + result.overCapacityPenalty());
    assertEquals(0.0, result.overCapacityPenalty());

    int totalSeated =
        service.roomSeating(examId).stream().mapToInt(RoomSeatingView::assignedStudents).sum();
    assertEquals(20, totalSeated);
  }

  @Test
  void deterministicGreedyFillsFirstRoomFirst() {
    long examId =
        service.createExam(COURSE_ID, "Završni", "ZI", "ZAVRSNI", LocalDateTime.now(), 120, 40.0);
    service.reserveRoom(examId, roomA, 15, 2);
    service.reserveRoom(examId, roomB, 15, 2);
    service.registerEnrolledStudents(examId, COURSE_ID);

    SeatingResult result = service.generateSeating(examId, SeatingStrategy.SORTED_GREEDY);
    assertTrue(result.feasible());

    var roomViews = service.roomSeating(examId);
    // 20 students, 15 cap each: greedy fills first room to 15, second gets 5.
    assertEquals(15, roomViews.get(0).assignedStudents());
    assertEquals(5, roomViews.get(1).assignedStudents());
  }

  @Test
  void comparesAllAlgorithmsAndSortsBestFirst() {
    long examId =
        service.createExam(
            COURSE_ID, "Usporedba", "U", "MEDJUISPIT", LocalDateTime.now(), 90, 20.0);
    service.reserveRoom(examId, roomA, 15, 2);
    service.reserveRoom(examId, roomB, 15, 2);
    service.registerEnrolledStudents(examId, COURSE_ID);

    var runs = service.compareSeatingAlgorithms(examId);

    assertEquals(6, runs.size());
    for (int i = 1; i < runs.size(); i++) {
      assertTrue(
          runs.get(i - 1).penalty() <= runs.get(i).penalty(),
          "results must be sorted by penalty ascending");
    }
    assertTrue(runs.get(0).feasible(), "best algorithm should find a feasible seating");
    assertTrue(runs.get(0).penaltyHistory().size() > 1, "convergence curve expected");
  }

  @Test
  void generatesSeatingWithANamedMetaheuristic() {
    long examId =
        service.createExam(COURSE_ID, "PSO", "P", "MEDJUISPIT", LocalDateTime.now(), 90, 20.0);
    service.reserveRoom(examId, roomA, 15, 2);
    service.reserveRoom(examId, roomB, 15, 2);
    service.registerEnrolledStudents(examId, COURSE_ID);

    SeatingResult result = service.generateSeatingWith(examId, "PARTICLE_SWARM");

    assertEquals("PARTICLE_SWARM", result.strategy());
    assertEquals(20, result.seatedStudents());
    assertTrue(result.feasible());
  }

  @Test
  void publishMarksTheExamPublished() {
    long examId =
        service.createExam(
            COURSE_ID, "Kratka", "KP", "KRATKA_PROVJERA", LocalDateTime.now(), 30, 5.0);
    service.publish(examId);
    assertTrue(exams.findById(examId).orElseThrow().published());
  }

  @Test
  void publishNotifiesRegisteredStudentsByEmail() {
    long examId =
        service.createExam(
            COURSE_ID, "Obavijest", "OB", "MEDJUISPIT", LocalDateTime.now(), 60, 10.0);
    service.registerEnrolledStudents(examId, COURSE_ID);

    service.publish(examId);

    assertEquals(1, mail.calls);
    assertEquals(20, mail.lastRecipients.size());
    assertTrue(mail.lastRecipients.contains("s0@fer.hr"));
    assertTrue(mail.lastSubject.contains("OB"));
  }

  @Test
  void publishWithoutRegistrationsSendsNoMail() {
    long examId =
        service.createExam(COURSE_ID, "Prazna", "PR", "MEDJUISPIT", LocalDateTime.now(), 60, 10.0);
    service.publish(examId);
    assertEquals(0, mail.calls);
  }

  @Test
  void listExamsReportsRegistrationAndRoomCounts() {
    long examId =
        service.createExam(COURSE_ID, "Test", "T", "MEDJUISPIT", LocalDateTime.now(), 60, 10.0);
    service.reserveRoom(examId, roomA, 15, 2);
    service.registerEnrolledStudents(examId, COURSE_ID);

    ExamView view =
        service.listExams(COURSE_ID).stream()
            .filter(e -> e.id() == examId)
            .findFirst()
            .orElseThrow();
    assertEquals(20, view.registeredStudents());
    assertEquals(1, view.reservedRooms());
    assertEquals(15, view.totalRoomCapacity());
  }

  /** Captures the most recent send for assertions. */
  private static final class RecordingMailSender
      implements hr.fer.zemris.ferko.application.port.MailSender {
    private int calls;
    private java.util.List<String> lastRecipients = java.util.List.of();
    private String lastSubject = "";

    @Override
    public void send(java.util.List<String> recipients, String subject, String body) {
      calls++;
      lastRecipients = recipients;
      lastSubject = subject;
    }
  }
}
