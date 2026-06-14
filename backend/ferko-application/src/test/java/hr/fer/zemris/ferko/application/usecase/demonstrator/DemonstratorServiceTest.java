package hr.fer.zemris.ferko.application.usecase.demonstrator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.DemonstratorRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.Demonstrator;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Student;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DemonstratorServiceTest {

  private final InMemoryAcademicRepositories.Courses courses =
      new InMemoryAcademicRepositories.Courses();
  private final InMemoryAcademicRepositories.Students students =
      new InMemoryAcademicRepositories.Students();
  private final InMemoryAcademicRepositories.Users users = new InMemoryAcademicRepositories.Users();
  private final FakeDemonstrators demonstrators = new FakeDemonstrators();
  private final DemonstratorService service =
      new DemonstratorService(demonstrators, students, users, courses);

  @Test
  void assignsListsAndRemovesDemonstrators() {
    long courseId = courses.save(new Course(0L, "A", "Algebra", "2026LJ", 5, "", "")).id();
    long userId =
        users
            .save(
                new AppUser(
                    0L,
                    "ana",
                    "h",
                    "Ana Studentica",
                    "a@fer.hr",
                    true,
                    LocalDateTime.now(),
                    Set.of(Role.STUDENT)))
            .id();
    long studentId = students.save(new Student(0L, userId, "0036000001", "R", 2)).id();

    assertTrue(service.assignByJmbag(courseId, "0036000001"));
    // Idempotent.
    service.assign(courseId, studentId);

    var list = service.listForCourse(courseId);
    assertEquals(1, list.size());
    assertEquals("Ana Studentica", list.get(0).fullName());
    assertEquals("0036000001", list.get(0).jmbag());

    assertEquals(1, service.myDuties("ana").size());
    assertEquals("A", service.myDuties("ana").get(0).courseCode());

    assertTrue(service.remove(courseId, studentId));
    assertTrue(service.listForCourse(courseId).isEmpty());
  }

  @Test
  void assignUnknownJmbagReturnsFalse() {
    long courseId = courses.save(new Course(0L, "A", "Algebra", "2026LJ", 5, "", "")).id();
    assertFalse(service.assignByJmbag(courseId, "9999999999"));
  }

  private static final class FakeDemonstrators implements DemonstratorRepository {
    private final List<Demonstrator> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public Demonstrator save(Demonstrator d) {
      Demonstrator saved = new Demonstrator(++seq, d.courseId(), d.studentId());
      store.add(saved);
      return saved;
    }

    @Override
    public List<Demonstrator> findByCourse(long courseId) {
      return store.stream().filter(d -> d.courseId() == courseId).toList();
    }

    @Override
    public List<Demonstrator> findByStudent(long studentId) {
      return store.stream().filter(d -> d.studentId() == studentId).toList();
    }

    @Override
    public boolean exists(long courseId, long studentId) {
      return store.stream().anyMatch(d -> d.courseId() == courseId && d.studentId() == studentId);
    }

    @Override
    public boolean delete(long courseId, long studentId) {
      return store.removeIf(d -> d.courseId() == courseId && d.studentId() == studentId);
    }
  }
}
