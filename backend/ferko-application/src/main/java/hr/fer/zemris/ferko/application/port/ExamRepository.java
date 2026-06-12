package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamRegistration;
import hr.fer.zemris.ferko.domain.model.ExamRoom;
import hr.fer.zemris.ferko.domain.model.ExamSeat;
import java.util.List;
import java.util.Optional;

/** Persistence port for assessments together with their registrations, rooms and seating. */
public interface ExamRepository {

  Exam save(Exam exam);

  Optional<Exam> findById(long id);

  List<Exam> findByCourse(long courseId);

  ExamRegistration addRegistration(ExamRegistration registration);

  List<ExamRegistration> findRegistrations(long examId);

  ExamRoom addRoom(ExamRoom room);

  List<ExamRoom> findRooms(long examId);

  /** Replaces the full seating of an exam (idempotent re-scheduling). */
  void replaceSeats(long examId, List<ExamSeat> seats);

  List<ExamSeat> findSeats(long examId);

  void markPublished(long examId, boolean published);
}
