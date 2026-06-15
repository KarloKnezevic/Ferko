package hr.fer.zemris.ferko.application.usecase.notice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.port.NoticeRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import hr.fer.zemris.ferko.application.usecase.academic.AcademicProvisioningService;
import hr.fer.zemris.ferko.application.usecase.access.AccessControlService;
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import hr.fer.zemris.ferko.domain.model.Notice;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NoticeServiceTest {

  /** Minimal in-memory repository for the service test. */
  private static final class FakeNoticeRepository implements NoticeRepository {
    private final List<Notice> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public Notice save(Notice notice) {
      Notice saved =
          new Notice(
              ++seq,
              notice.courseId(),
              notice.title(),
              notice.body(),
              notice.authorName(),
              notice.createdAt(),
              notice.pinned());
      store.add(saved);
      return saved;
    }

    @Override
    public List<Notice> findRecent(int limit) {
      return store.stream()
          .sorted(
              Comparator.comparing(Notice::pinned)
                  .reversed()
                  .thenComparing(Comparator.comparing(Notice::createdAt).reversed()))
          .limit(limit)
          .toList();
    }

    @Override
    public List<Notice> findByCourse(long courseId) {
      return store.stream().filter(n -> Long.valueOf(courseId).equals(n.courseId())).toList();
    }

    @Override
    public Optional<Notice> findById(long id) {
      return store.stream().filter(n -> n.id() == id).findFirst();
    }

    @Override
    public boolean deleteById(long id) {
      return store.removeIf(n -> n.id() == id);
    }
  }

  /** Class schedule is irrelevant to notices; this fake satisfies the provisioning constructor. */
  private static final class NoopSchedule implements ClassScheduleRepository {
    @Override
    public ClassSchedule save(ClassSchedule entry) {
      return entry;
    }

    @Override
    public List<ClassSchedule> findByCourse(long courseId) {
      return List.of();
    }

    @Override
    public List<ClassSchedule> findAll() {
      return List.of();
    }

    @Override
    public java.util.Optional<ClassSchedule> findById(long id) {
      return java.util.Optional.empty();
    }

    @Override
    public boolean updatePlacement(
        long id,
        java.time.DayOfWeek dayOfWeek,
        java.time.LocalTime startsAt,
        java.time.LocalTime endsAt,
        Long roomId) {
      return false;
    }

    @Override
    public int deleteByCourse(long courseId) {
      return 0;
    }
  }

  private FakeNoticeRepository notices;
  private NoticeService service;
  private long courseId;

  @BeforeEach
  void setUp() {
    InMemoryAcademicRepositories.Users users = new InMemoryAcademicRepositories.Users();
    InMemoryAcademicRepositories.Semesters semesters = new InMemoryAcademicRepositories.Semesters();
    InMemoryAcademicRepositories.Courses courses = new InMemoryAcademicRepositories.Courses();
    InMemoryAcademicRepositories.Students students = new InMemoryAcademicRepositories.Students();
    InMemoryAcademicRepositories.Enrollments enrollments =
        new InMemoryAcademicRepositories.Enrollments();
    InMemoryAcademicRepositories.Rooms rooms = new InMemoryAcademicRepositories.Rooms();
    AcademicProvisioningService provisioning =
        new AcademicProvisioningService(
            semesters, courses, enrollments, students, rooms, users, new NoopSchedule());

    provisioning.provisionSemester(
        "2026LJ",
        "2025/2026",
        "LJETNI",
        LocalDate.parse("2026-03-01"),
        LocalDate.parse("2026-07-15"),
        true);
    courseId = provisioning.provisionCourse("PROG", "Programiranje", "2026LJ", 6, "o", "l");
    long lecturerId =
        provisioning.provisionStaffUser(
            "lecturer.marko", "h", "Marko P", "m@fer.hr", Set.of("NOSITELJ"), LocalDateTime.now());
    provisioning.assignStaff(courseId, lecturerId, "NOSITELJ");
    long studentId =
        provisioning.provisionStudent(
            "0036501020", "h", "Ana A", "ana@fer.hr", "0036501020", "R", 1, LocalDateTime.now());
    provisioning.enroll(studentId, courseId, LocalDateTime.now());

    AccessControlService access = new AccessControlService(users, students, enrollments, courses);
    notices = new FakeNoticeRepository();
    service = new NoticeService(notices, access);
  }

  @Test
  void canDeleteFlagReflectsRoleAndTeachingRelationship() {
    service.publish(null, "Fakultetska", "tekst", true, "admin.ferko");
    service.publish(courseId, "Kolegij", "tekst", false, "lecturer.marko");

    // Admin (global): may delete both.
    List<NoticeView> asAdmin = service.recent(10, "admin.ferko", Set.of("ADMIN"));
    assertTrue(asAdmin.stream().allMatch(NoticeView::canDelete));

    // Lecturer: may delete the course notice, not the faculty-wide one.
    List<NoticeView> asLecturer = service.recent(10, "lecturer.marko", Set.of("NOSITELJ"));
    NoticeView faculty =
        asLecturer.stream().filter(n -> n.courseId() == null).findFirst().orElseThrow();
    NoticeView course =
        asLecturer.stream().filter(n -> n.courseId() != null).findFirst().orElseThrow();
    assertFalse(faculty.canDelete());
    assertTrue(course.canDelete());

    // Student: may delete neither.
    List<NoticeView> asStudent = service.recent(10, "0036501020", Set.of("STUDENT"));
    assertTrue(asStudent.stream().noneMatch(NoticeView::canDelete));
  }

  @Test
  void deleteEnforcesAuthorizationAndReportsOutcomes() {
    long facultyId = service.publish(null, "Fakultetska", "tekst", true, "admin.ferko");
    long courseNoticeId = service.publish(courseId, "Kolegij", "tekst", false, "lecturer.marko");

    // Missing notice.
    assertEquals(
        NoticeService.DeleteOutcome.NOT_FOUND,
        service.delete(9999L, "admin.ferko", Set.of("ADMIN")));
    // Lecturer cannot delete a faculty-wide notice.
    assertEquals(
        NoticeService.DeleteOutcome.FORBIDDEN,
        service.delete(facultyId, "lecturer.marko", Set.of("NOSITELJ")));
    // Student cannot delete a course notice.
    assertEquals(
        NoticeService.DeleteOutcome.FORBIDDEN,
        service.delete(courseNoticeId, "0036501020", Set.of("STUDENT")));
    // Lecturer deletes their course notice.
    assertEquals(
        NoticeService.DeleteOutcome.DELETED,
        service.delete(courseNoticeId, "lecturer.marko", Set.of("NOSITELJ")));
    // Admin deletes the faculty-wide notice.
    assertEquals(
        NoticeService.DeleteOutcome.DELETED,
        service.delete(facultyId, "admin.ferko", Set.of("ADMIN")));

    assertTrue(service.recent(10, "admin.ferko", Set.of("ADMIN")).isEmpty());
  }

  @Test
  void recentLimitIsClampedToAtLeastOne() {
    service.publish(null, "A", "x", false, "admin.ferko");
    service.publish(null, "B", "x", false, "admin.ferko");
    assertEquals(1, service.recent(0, "admin.ferko", Set.of("ADMIN")).size());
  }
}
