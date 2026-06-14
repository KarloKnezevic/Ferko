package hr.fer.zemris.ferko.application.usecase.exam;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.application.port.ExamAssistantRepository;
import hr.fer.zemris.ferko.application.port.ExamRepository;
import hr.fer.zemris.ferko.application.port.RoomRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.ExamRoomAssistant;
import hr.fer.zemris.ferko.domain.model.Room;
import java.util.List;
import java.util.Optional;

/**
 * Assigns invigilators ("dežurni") to the rooms reserved for an assessment — the "Dodjela
 * asistenata" step of the FERKO scheduling flow.
 */
public class ExamAssistantService {

  private final ExamAssistantRepository assistantRepository;
  private final ExamRepository examRepository;
  private final RoomRepository roomRepository;
  private final AppUserRepository userRepository;

  public ExamAssistantService(
      ExamAssistantRepository assistantRepository,
      ExamRepository examRepository,
      RoomRepository roomRepository,
      AppUserRepository userRepository) {
    this.assistantRepository = assistantRepository;
    this.examRepository = examRepository;
    this.roomRepository = roomRepository;
    this.userRepository = userRepository;
  }

  /**
   * Assigns the user identified by {@code username} as an invigilator of {@code roomId} for the
   * exam. Returns {@code false} when the user does not exist, the room is not reserved for the
   * exam, or the user is already assigned to that room (idempotent).
   */
  public boolean assignByUsername(long examId, long roomId, String username) {
    Optional<AppUser> user = userRepository.findByUsername(username);
    if (user.isEmpty()) {
      return false;
    }
    boolean roomReserved =
        examRepository.findRooms(examId).stream().anyMatch(room -> room.roomId() == roomId);
    if (!roomReserved) {
      return false;
    }
    long userId = user.get().id();
    boolean alreadyAssigned =
        assistantRepository.findByExam(examId).stream()
            .anyMatch(a -> a.roomId() == roomId && a.userId() == userId);
    if (alreadyAssigned) {
      return false;
    }
    assistantRepository.assign(new ExamRoomAssistant(0L, examId, roomId, userId));
    return true;
  }

  public List<ExamAssistantView> listForExam(long examId) {
    return assistantRepository.findByExam(examId).stream().map(this::toView).toList();
  }

  public void remove(long examId, long assignmentId) {
    assistantRepository.remove(examId, assignmentId);
  }

  private ExamAssistantView toView(ExamRoomAssistant assignment) {
    String roomCode = roomRepository.findById(assignment.roomId()).map(Room::code).orElse("");
    AppUser user = userRepository.findById(assignment.userId()).orElse(null);
    String username = user == null ? "" : user.username();
    String fullName = user == null ? "" : user.fullName();
    return new ExamAssistantView(
        assignment.id(),
        assignment.examId(),
        assignment.roomId(),
        roomCode,
        assignment.userId(),
        username,
        fullName);
  }
}
