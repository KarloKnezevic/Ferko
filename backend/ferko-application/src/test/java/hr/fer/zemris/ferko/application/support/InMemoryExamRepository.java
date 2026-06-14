package hr.fer.zemris.ferko.application.support;

import hr.fer.zemris.ferko.application.port.ExamRepository;
import hr.fer.zemris.ferko.domain.model.Exam;
import hr.fer.zemris.ferko.domain.model.ExamRegistration;
import hr.fer.zemris.ferko.domain.model.ExamRoom;
import hr.fer.zemris.ferko.domain.model.ExamSeat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** In-memory fake of {@link ExamRepository} for use-case tests. */
public final class InMemoryExamRepository implements ExamRepository {

  private final List<Exam> exams = new ArrayList<>();
  private final List<ExamRegistration> registrations = new ArrayList<>();
  private final List<ExamRoom> rooms = new ArrayList<>();
  private final List<ExamSeat> seats = new ArrayList<>();
  private final AtomicLong seq = new AtomicLong(0);

  @Override
  public Exam save(Exam exam) {
    long id = exam.id() <= 0 ? seq.incrementAndGet() : exam.id();
    Exam stored =
        new Exam(
            id,
            exam.courseId(),
            exam.title(),
            exam.shortName(),
            exam.kind(),
            exam.startsAt(),
            exam.durationMinutes(),
            exam.maxPoints(),
            exam.ordinal(),
            exam.visibility(),
            exam.locked(),
            exam.prerequisiteFlagId(),
            exam.published());
    exams.removeIf(existing -> existing.id() == id);
    exams.add(stored);
    return stored;
  }

  @Override
  public Optional<Exam> findById(long id) {
    return exams.stream().filter(e -> e.id() == id).findFirst();
  }

  @Override
  public List<Exam> findByCourse(long courseId) {
    return exams.stream().filter(e -> e.courseId() == courseId).toList();
  }

  @Override
  public ExamRegistration addRegistration(ExamRegistration registration) {
    ExamRegistration stored =
        new ExamRegistration(
            seq.incrementAndGet(),
            registration.examId(),
            registration.studentId(),
            registration.registeredAt(),
            registration.status());
    registrations.add(stored);
    return stored;
  }

  @Override
  public void removeRegistration(long examId, long studentId) {
    registrations.removeIf(r -> r.examId() == examId && r.studentId() == studentId);
  }

  @Override
  public List<ExamRegistration> findRegistrations(long examId) {
    return registrations.stream().filter(r -> r.examId() == examId).toList();
  }

  @Override
  public ExamRoom addRoom(ExamRoom room) {
    ExamRoom stored =
        new ExamRoom(
            seq.incrementAndGet(),
            room.examId(),
            room.roomId(),
            room.capacity(),
            room.requiredAssistants(),
            room.reserved());
    rooms.add(stored);
    return stored;
  }

  @Override
  public List<ExamRoom> findRooms(long examId) {
    return rooms.stream().filter(r -> r.examId() == examId).toList();
  }

  @Override
  public void replaceSeats(long examId, List<ExamSeat> newSeats) {
    seats.removeIf(s -> s.examId() == examId);
    for (ExamSeat seat : newSeats) {
      seats.add(
          new ExamSeat(
              seq.incrementAndGet(),
              seat.examId(),
              seat.studentId(),
              seat.roomId(),
              seat.seatNo(),
              seat.testGroup()));
    }
  }

  @Override
  public List<ExamSeat> findSeats(long examId) {
    return seats.stream().filter(s -> s.examId() == examId).toList();
  }

  @Override
  public void markPublished(long examId, boolean published) {
    findById(examId)
        .ifPresent(
            exam ->
                save(
                    new Exam(
                        exam.id(),
                        exam.courseId(),
                        exam.title(),
                        exam.shortName(),
                        exam.kind(),
                        exam.startsAt(),
                        exam.durationMinutes(),
                        exam.maxPoints(),
                        exam.ordinal(),
                        exam.visibility(),
                        exam.locked(),
                        exam.prerequisiteFlagId(),
                        published)));
  }
}
