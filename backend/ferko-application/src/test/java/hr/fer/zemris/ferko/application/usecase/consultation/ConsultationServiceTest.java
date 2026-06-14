package hr.fer.zemris.ferko.application.usecase.consultation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ConsultationRepository;
import hr.fer.zemris.ferko.domain.model.Consultation;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConsultationServiceTest {

  private static final class FakeRepository implements ConsultationRepository {
    private final List<Consultation> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public Consultation save(Consultation c) {
      Consultation saved =
          new Consultation(
              ++seq,
              c.courseId(),
              c.staffName(),
              c.dayOfWeek(),
              c.startsAt(),
              c.endsAt(),
              c.location());
      store.add(saved);
      return saved;
    }

    @Override
    public List<Consultation> findByCourse(long courseId) {
      return store.stream().filter(c -> c.courseId() == courseId).toList();
    }

    @Override
    public void remove(long courseId, long consultationId) {
      store.removeIf(c -> c.courseId() == courseId && c.id() == consultationId);
    }
  }

  @Test
  void addsAndFormatsConsultations() {
    ConsultationService service = new ConsultationService(new FakeRepository());
    long id = service.add(5L, "Marko Predavač", "Ponedjeljak", "10:00", "11:30", "C-04");
    assertTrue(id > 0);

    List<ConsultationView> list = service.forCourse(5L);
    assertEquals(1, list.size());
    ConsultationView view = list.get(0);
    assertEquals("Ponedjeljak", view.dayOfWeek());
    assertEquals("10:00", view.startsAt());
    assertEquals("11:30", view.endsAt());
    assertEquals("C-04", view.location());
    assertEquals("Marko Predavač", view.staffName());
    assertTrue(service.forCourse(999L).isEmpty());
  }

  @Test
  void removesConsultation() {
    ConsultationService service = new ConsultationService(new FakeRepository());
    long id = service.add(5L, "X", "Utorak", "09:00", "10:00", "");
    service.remove(5L, id);
    assertTrue(service.forCourse(5L).isEmpty());
  }

  @Test
  void rejectsBlankDayBadTimeAndNonPositiveInterval() {
    ConsultationService service = new ConsultationService(new FakeRepository());
    assertThrows(
        IllegalArgumentException.class, () -> service.add(5L, "X", "  ", "10:00", "11:00", ""));
    assertThrows(
        IllegalArgumentException.class, () -> service.add(5L, "X", "Pon", "deset", "11:00", ""));
    assertThrows(
        IllegalArgumentException.class, () -> service.add(5L, "X", "Pon", "11:00", "10:00", ""));
  }
}
