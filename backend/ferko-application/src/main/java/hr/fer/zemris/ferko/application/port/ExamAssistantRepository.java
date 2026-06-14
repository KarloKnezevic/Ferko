package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.ExamRoomAssistant;
import java.util.List;

/** Persistence port for invigilator assignments ("dežurstva") per exam room. */
public interface ExamAssistantRepository {

  ExamRoomAssistant assign(ExamRoomAssistant assignment);

  /** Assignments for an exam, ordered by room then id. */
  List<ExamRoomAssistant> findByExam(long examId);

  /** All assignments of a user across exams ("moja dežurstva"). */
  List<ExamRoomAssistant> findByUser(long userId);

  /** Removes a single assignment of the given exam (no-op if it does not belong to it). */
  void remove(long examId, long assignmentId);
}
