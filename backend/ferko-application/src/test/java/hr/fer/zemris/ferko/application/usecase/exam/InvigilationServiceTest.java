package hr.fer.zemris.ferko.application.usecase.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ExamAssistantRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.support.InMemoryExamRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamKind;
import hr.fer.zemris.ferko.domain.model.ExamRoomAssistant;
import hr.fer.zemris.ferko.domain.model.ExamVisibility;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Room;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InvigilationServiceTest {

  private InMemoryExamRepository exams;
  private InMemoryAcademicRepositories.Courses courses;
  private InMemoryAcademicRepositories.Rooms rooms;
  private InMemoryAcademicRepositories.Users users;
  private InMemoryAssistants assistants;
  private InvigilationService service;

  @BeforeEach
  void setUp() {
    exams = new InMemoryExamRepository();
    courses = new InMemoryAcademicRepositories.Courses();
    rooms = new InMemoryAcademicRepositories.Rooms();
    users = new InMemoryAcademicRepositories.Users();
    assistants = new InMemoryAssistants();
    service = new InvigilationService(assistants, exams, courses, rooms, users);
  }

  @Test
  void listsDutiesWithExamCourseAndRoomDetails() {
    AppUser iva =
        users.save(
            new AppUser(
                0L,
                "asistent.iva",
                "x",
                "Iva Asistent",
                "iva@fer.hr",
                true,
                LocalDateTime.now(),
                Set.of(Role.ASISTENT)));
    long courseId =
        courses.save(new Course(0L, "PROG", "Programiranje", "2025/26-ZS", 6, "", "")).id();
    long roomId = rooms.save(new Room(0L, "A101", "Zgrada A", 60, 2, false)).id();
    long examId =
        exams
            .save(
                new Exam(
                    0L,
                    courseId,
                    "Međuispit",
                    "MI1",
                    ExamKind.MEDJUISPIT,
                    LocalDateTime.now().plusDays(2),
                    90,
                    20.0,
                    0,
                    ExamVisibility.ALWAYS,
                    false,
                    null,
                    false))
            .id();
    assistants.assign(new ExamRoomAssistant(0L, examId, roomId, iva.id()));

    List<MyDutyView> duties = service.myDuties("asistent.iva");
    assertEquals(1, duties.size());
    MyDutyView duty = duties.get(0);
    assertEquals("MI1", duty.examShortName());
    assertEquals("PROG", duty.courseCode());
    assertEquals("Programiranje", duty.courseName());
    assertEquals("A101", duty.roomCode());
    assertEquals(examId, duty.examId());
  }

  @Test
  void emptyForUserWithoutDutiesAndUnknownUser() {
    users.save(
        new AppUser(
            0L,
            "asistent.iva",
            "x",
            "Iva",
            "iva@fer.hr",
            true,
            LocalDateTime.now(),
            Set.of(Role.ASISTENT)));
    assertTrue(service.myDuties("asistent.iva").isEmpty());
    assertTrue(service.myDuties("ne.postoji").isEmpty());
  }

  private static final class InMemoryAssistants implements ExamAssistantRepository {
    private final List<ExamRoomAssistant> data = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public ExamRoomAssistant assign(ExamRoomAssistant a) {
      ExamRoomAssistant stored =
          new ExamRoomAssistant(seq.incrementAndGet(), a.examId(), a.roomId(), a.userId());
      data.add(stored);
      return stored;
    }

    @Override
    public List<ExamRoomAssistant> findByExam(long examId) {
      return data.stream().filter(a -> a.examId() == examId).toList();
    }

    @Override
    public List<ExamRoomAssistant> findByUser(long userId) {
      return data.stream().filter(a -> a.userId() == userId).toList();
    }

    @Override
    public void remove(long examId, long assignmentId) {
      data.removeIf(a -> a.examId() == examId && a.id() == assignmentId);
    }
  }
}
