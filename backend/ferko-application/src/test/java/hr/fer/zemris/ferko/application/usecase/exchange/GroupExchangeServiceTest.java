package hr.fer.zemris.ferko.application.usecase.exchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.GroupExchangeRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.ExchangeStatus;
import hr.fer.zemris.ferko.domain.model.GroupExchangeRequest;
import hr.fer.zemris.ferko.domain.model.GroupType;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Student;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GroupExchangeServiceTest {

  private static final class FakeExchange implements GroupExchangeRepository {
    private final List<GroupExchangeRequest> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public GroupExchangeRequest save(GroupExchangeRequest r) {
      GroupExchangeRequest saved =
          new GroupExchangeRequest(
              ++seq,
              r.courseId(),
              r.studentId(),
              r.fromGroupId(),
              r.toGroupId(),
              r.status(),
              r.reason(),
              r.decidedBy(),
              r.createdAt(),
              r.decidedAt());
      store.add(saved);
      return saved;
    }

    @Override
    public List<GroupExchangeRequest> findByCourse(long courseId) {
      return store.stream().filter(r -> r.courseId() == courseId).toList();
    }

    @Override
    public Optional<GroupExchangeRequest> findById(long id) {
      return store.stream().filter(r -> r.id() == id).findFirst();
    }

    @Override
    public void updateDecision(
        long id, ExchangeStatus status, String decidedBy, LocalDateTime decidedAt) {
      for (int i = 0; i < store.size(); i++) {
        GroupExchangeRequest r = store.get(i);
        if (r.id() == id) {
          store.set(
              i,
              new GroupExchangeRequest(
                  r.id(),
                  r.courseId(),
                  r.studentId(),
                  r.fromGroupId(),
                  r.toGroupId(),
                  status,
                  r.reason(),
                  decidedBy,
                  r.createdAt(),
                  decidedAt));
        }
      }
    }
  }

  private InMemoryAcademicRepositories.Users users;
  private InMemoryAcademicRepositories.Students students;
  private InMemoryAcademicRepositories.Courses courses;
  private GroupExchangeService service;

  private long courseId;
  private long g1;
  private long g2;

  @BeforeEach
  void setUp() {
    users = new InMemoryAcademicRepositories.Users();
    students = new InMemoryAcademicRepositories.Students();
    courses = new InMemoryAcademicRepositories.Courses();
    service = new GroupExchangeService(new FakeExchange(), users, students, courses);

    long userId =
        users
            .save(
                new AppUser(
                    0L,
                    "student.x",
                    "h",
                    "Iks Igrek",
                    "x@fer.hr",
                    true,
                    LocalDateTime.now(),
                    EnumSet.of(Role.STUDENT)))
            .id();
    students.save(new Student(0L, userId, "0036599999", "Računarstvo", 2));
    courseId = 42L;
    g1 = courses.addGroup(new StudentGroup(0L, courseId, "P1", GroupType.LECTURE, "Pon", 100)).id();
    g2 = courses.addGroup(new StudentGroup(0L, courseId, "P2", GroupType.LECTURE, "Uto", 100)).id();
  }

  @Test
  void studentRequestsAndStaffApproves() {
    long id = service.request(courseId, "student.x", g1, g2, "Posao");
    assertTrue(id > 0);

    List<GroupExchangeView> list = service.listForCourse(courseId);
    assertEquals(1, list.size());
    assertEquals("Iks Igrek", list.get(0).studentName());
    assertEquals("P1", list.get(0).fromGroup());
    assertEquals("P2", list.get(0).toGroup());
    assertEquals("PENDING", list.get(0).status());

    service.decide(id, true, "stuslu.sara");
    assertEquals("APPROVED", service.listForCourse(courseId).get(0).status());
  }

  @Test
  void nonStudentCannotRequest() {
    assertThrows(
        IllegalArgumentException.class, () -> service.request(courseId, "nepostoji", g1, g2, "x"));
  }
}
