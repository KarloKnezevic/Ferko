package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.Consultation;
import java.util.List;

/** Persistence port for office-hours slots ("konzultacije"). */
public interface ConsultationRepository {

  Consultation save(Consultation consultation);

  /** Office-hours slots for a course, ordered by start time then id. */
  List<Consultation> findByCourse(long courseId);

  /** Removes a slot of the given course (no-op if it does not belong to it). */
  void remove(long courseId, long consultationId);
}
