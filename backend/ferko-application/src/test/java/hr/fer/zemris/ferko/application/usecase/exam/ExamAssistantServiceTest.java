package hr.fer.zemris.ferko.application.usecase.exam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ExamAssistantRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.support.InMemoryExamRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.ExamRoom;
import hr.fer.zemris.ferko.domain.model.ExamRoomAssistant;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Room;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExamAssistantServiceTest {

  private InMemoryExamRepository exams;
  private InMemoryAcademicRepositories.Rooms rooms;
  private InMemoryAcademicRepositories.Users users;
  private ExamAssistantService service;
  private long examId;

  @BeforeEach
  void setUp() {
    exams = new InMemoryExamRepository();
    rooms = new InMemoryAcademicRepositories.Rooms();
    users = new InMemoryAcademicRepositories.Users();
    service = new ExamAssistantService(new InMemoryAssistants(), exams, rooms, users);

    Room room = rooms.save(new Room(0L, "A101", "Zgrada A", 60, 2, false));
    users.save(
        new AppUser(
            0L,
            "assistant.iva",
            "x",
            "Iva Asistent",
            "iva@fer.hr",
            true,
            LocalDateTime.now(),
            Set.of(Role.ASISTENT)));
    examId = 7L;
    exams.addRoom(new ExamRoom(0L, examId, room.id(), 60, 2, true));
  }

  @Test
  void assignsAndListsInvigilator() {
    long roomId = exams.findRooms(examId).get(0).roomId();
    assertTrue(service.assignByUsername(examId, roomId, "assistant.iva"));

    List<ExamAssistantView> assigned = service.listForExam(examId);
    assertEquals(1, assigned.size());
    assertEquals("assistant.iva", assigned.get(0).username());
    assertEquals("Iva Asistent", assigned.get(0).fullName());
    assertEquals("A101", assigned.get(0).roomCode());
  }

  @Test
  void rejectsUnknownUserUnreservedRoomAndDuplicate() {
    long roomId = exams.findRooms(examId).get(0).roomId();
    assertFalse(service.assignByUsername(examId, roomId, "ne.postoji"));
    assertFalse(service.assignByUsername(examId, 999L, "assistant.iva"));

    assertTrue(service.assignByUsername(examId, roomId, "assistant.iva"));
    assertFalse(service.assignByUsername(examId, roomId, "assistant.iva"));
  }

  @Test
  void removesAssignment() {
    long roomId = exams.findRooms(examId).get(0).roomId();
    service.assignByUsername(examId, roomId, "assistant.iva");
    long assignmentId = service.listForExam(examId).get(0).id();

    service.remove(examId, assignmentId);
    assertTrue(service.listForExam(examId).isEmpty());
  }

  /** Minimal in-memory {@link ExamAssistantRepository} for the service test. */
  private static final class InMemoryAssistants implements ExamAssistantRepository {
    private final List<ExamRoomAssistant> data = new ArrayList<>();
    private final AtomicLong seq = new AtomicLong(0);

    @Override
    public ExamRoomAssistant assign(ExamRoomAssistant assignment) {
      ExamRoomAssistant stored =
          new ExamRoomAssistant(
              seq.incrementAndGet(), assignment.examId(), assignment.roomId(), assignment.userId());
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
