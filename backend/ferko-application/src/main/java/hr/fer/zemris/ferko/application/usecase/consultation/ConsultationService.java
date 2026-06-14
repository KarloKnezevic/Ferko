package hr.fer.zemris.ferko.application.usecase.consultation;

import hr.fer.zemris.ferko.application.port.ConsultationRepository;
import hr.fer.zemris.ferko.domain.model.Consultation;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Manages weekly office-hours slots ("konzultacije") published by teaching staff. */
public class ConsultationService {

  private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

  private final ConsultationRepository repository;

  public ConsultationService(ConsultationRepository repository) {
    this.repository = repository;
  }

  public List<ConsultationView> forCourse(long courseId) {
    return repository.findByCourse(courseId).stream().map(ConsultationService::toView).toList();
  }

  /**
   * Adds an office-hours slot. {@code startsAt}/{@code endsAt} are {@code HH:mm}. Throws {@link
   * IllegalArgumentException} on a blank day, unparseable time, or a non-positive interval.
   */
  public long add(
      long courseId,
      String staffName,
      String dayOfWeek,
      String startsAt,
      String endsAt,
      String location) {
    if (dayOfWeek == null || dayOfWeek.isBlank()) {
      throw new IllegalArgumentException("Dan u tjednu je obavezan.");
    }
    LocalTime start = parse(startsAt);
    LocalTime end = parse(endsAt);
    if (!end.isAfter(start)) {
      throw new IllegalArgumentException("Kraj termina mora biti nakon početka.");
    }
    return repository
        .save(
            new Consultation(
                0L,
                courseId,
                staffName == null ? "" : staffName,
                dayOfWeek.trim(),
                start,
                end,
                location == null ? "" : location.trim()))
        .id();
  }

  public void remove(long courseId, long consultationId) {
    repository.remove(courseId, consultationId);
  }

  private static LocalTime parse(String time) {
    try {
      return LocalTime.parse(time, HM);
    } catch (DateTimeParseException | NullPointerException ex) {
      throw new IllegalArgumentException("Vrijeme mora biti u obliku HH:mm.");
    }
  }

  private static ConsultationView toView(Consultation consultation) {
    return new ConsultationView(
        consultation.id(),
        consultation.courseId(),
        consultation.staffName(),
        consultation.dayOfWeek(),
        consultation.startsAt().format(HM),
        consultation.endsAt().format(HM),
        consultation.location());
  }
}
