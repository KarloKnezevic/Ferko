package hr.fer.zemris.ferko.application.usecase.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.CourseComponentRepository;
import hr.fer.zemris.ferko.domain.model.CourseComponent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CourseComponentServiceTest {

  private static final class FakeRepository implements CourseComponentRepository {
    private final List<CourseComponent> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public CourseComponent save(CourseComponent c) {
      CourseComponent saved =
          new CourseComponent(
              ++seq, c.courseId(), c.title(), c.content(), c.ordinal(), c.visible());
      store.add(saved);
      return saved;
    }

    @Override
    public List<CourseComponent> findByCourse(long courseId) {
      return store.stream().filter(c -> c.courseId() == courseId).toList();
    }
  }

  @Test
  void forCourseHidesInvisibleButAllForCourseDoesNot() {
    CourseComponentService service = new CourseComponentService(new FakeRepository());
    long id = service.add(3L, "O kolegiju", "Opis", 0, true);
    assertTrue(id > 0);
    service.add(3L, "Skriveno", "tajna", 1, false);

    assertEquals(1, service.forCourse(3L).size());
    assertEquals("O kolegiju", service.forCourse(3L).get(0).title());
    assertEquals(2, service.allForCourse(3L).size());
  }

  @Test
  void rejectsBlankTitle() {
    CourseComponentService service = new CourseComponentService(new FakeRepository());
    assertThrows(IllegalArgumentException.class, () -> service.add(3L, "  ", "x", 0, true));
  }
}
