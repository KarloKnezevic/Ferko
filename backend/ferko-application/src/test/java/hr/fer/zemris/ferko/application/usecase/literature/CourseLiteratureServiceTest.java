package hr.fer.zemris.ferko.application.usecase.literature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.CourseLiteratureRepository;
import hr.fer.zemris.ferko.domain.model.CourseLiterature;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CourseLiteratureServiceTest {

  private static final class FakeRepository implements CourseLiteratureRepository {
    private final List<CourseLiterature> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public CourseLiterature save(CourseLiterature l) {
      CourseLiterature saved =
          new CourseLiterature(
              ++seq, l.courseId(), l.title(), l.author(), l.mandatory(), l.ordinal());
      store.add(saved);
      return saved;
    }

    @Override
    public List<CourseLiterature> findByCourse(long courseId) {
      return store.stream().filter(l -> l.courseId() == courseId).toList();
    }
  }

  @Test
  void addsRequiredAndRecommendedEntries() {
    CourseLiteratureService service = new CourseLiteratureService(new FakeRepository());
    long id = service.add(5L, "Uvod u programiranje", "I. Anić", true, 0);
    assertTrue(id > 0);
    service.add(5L, "Dodatna skripta", "M. Marić", false, 1);

    List<CourseLiteratureView> list = service.forCourse(5L);
    assertEquals(2, list.size());
    assertTrue(list.get(0).mandatory());
    assertEquals("Uvod u programiranje", list.get(0).title());
    assertEquals("I. Anić", list.get(0).author());
    assertFalse(list.get(1).mandatory());
    assertTrue(service.forCourse(999L).isEmpty());
  }

  @Test
  void trimsAndDefaultsAuthor() {
    CourseLiteratureService service = new CourseLiteratureService(new FakeRepository());
    service.add(7L, "  Knjiga  ", null, true, 0);
    CourseLiteratureView view = service.forCourse(7L).get(0);
    assertEquals("Knjiga", view.title());
    assertEquals("", view.author());
  }

  @Test
  void rejectsBlankTitle() {
    CourseLiteratureService service = new CourseLiteratureService(new FakeRepository());
    assertThrows(IllegalArgumentException.class, () -> service.add(1L, "  ", "x", true, 0));
  }
}
