package hr.fer.zemris.ferko.application.usecase.academic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.ClassScheduleRepository;
import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
// AppUserView is in this package; no import needed.
import hr.fer.zemris.ferko.domain.model.ClassSchedule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AcademicServicesTest {

  private static final class FakeSchedule implements ClassScheduleRepository {
    private final List<ClassSchedule> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public ClassSchedule save(ClassSchedule entry) {
      ClassSchedule saved =
          new ClassSchedule(
              ++seq,
              entry.courseId(),
              entry.groupId(),
              entry.type(),
              entry.roomId(),
              entry.dayOfWeek(),
              entry.startsAt(),
              entry.endsAt(),
              entry.instructor());
      store.add(saved);
      return saved;
    }

    @Override
    public List<ClassSchedule> findByCourse(long courseId) {
      return store.stream().filter(s -> s.courseId() == courseId).toList();
    }

    @Override
    public List<ClassSchedule> findAll() {
      return List.copyOf(store);
    }

    @Override
    public int deleteByCourse(long courseId) {
      int before = store.size();
      store.removeIf(s -> s.courseId() == courseId);
      return before - store.size();
    }
  }

  private InMemoryAcademicRepositories.Users users;
  private InMemoryAcademicRepositories.Semesters semesters;
  private InMemoryAcademicRepositories.Courses courses;
  private InMemoryAcademicRepositories.Students students;
  private InMemoryAcademicRepositories.Enrollments enrollments;
  private InMemoryAcademicRepositories.Rooms rooms;
  private AcademicProvisioningService provisioning;
  private AcademicQueryService query;

  @BeforeEach
  void setUp() {
    users = new InMemoryAcademicRepositories.Users();
    semesters = new InMemoryAcademicRepositories.Semesters();
    courses = new InMemoryAcademicRepositories.Courses();
    students = new InMemoryAcademicRepositories.Students();
    enrollments = new InMemoryAcademicRepositories.Enrollments();
    rooms = new InMemoryAcademicRepositories.Rooms();
    provisioning =
        new AcademicProvisioningService(
            semesters, courses, enrollments, students, rooms, users, new FakeSchedule());
    query = new AcademicQueryService(semesters, courses, enrollments, students, rooms, users);
  }

  @Test
  void provisionsAndQueriesTheAcademicGraph() {
    provisioning.provisionSemester(
        "2024Z",
        "2024/2025",
        "ZIMSKI",
        LocalDate.parse("2024-10-01"),
        LocalDate.parse("2025-02-15"),
        true);

    long roomId = provisioning.provisionRoom("A101", "Zgrada A", 120, 3, false);
    assertTrue(roomId > 0);

    long courseId =
        provisioning.provisionCourse("UURA", "Uvod u računarstvo", "2024Z", 6, "opis", "lit");
    long lecturerId =
        provisioning.provisionStaffUser(
            "lecturer.marko",
            "hash",
            "Marko Predavač",
            "marko@fer.hr",
            Set.of("NOSITELJ"),
            LocalDateTime.now());
    provisioning.assignStaff(courseId, lecturerId, "NOSITELJ");
    long groupId = provisioning.provisionGroup(courseId, "L01", "LAB", "Pon", 16);

    long studentId =
        provisioning.provisionStudent(
            "0036501001",
            "hash",
            "Ana Studentica",
            "ana@fer.hr",
            "0036501001",
            "Računarstvo",
            1,
            LocalDateTime.now());
    long enrollmentId = provisioning.enroll(studentId, courseId, LocalDateTime.now());
    provisioning.assignGroup(enrollmentId, groupId);

    assertEquals(1, query.listSemesters().size());
    assertEquals("2024Z", query.activeSemester().orElseThrow().code());

    assertEquals(1, query.listCourses("2024Z").size());
    assertEquals(1, query.listCourses(null).get(0).enrolledStudents());

    CourseDetailView detail = query.courseDetail(courseId).orElseThrow();
    assertEquals("UURA", detail.code());
    assertEquals(1, detail.staff().size());
    assertEquals("Marko Predavač", detail.staff().get(0).fullName());
    assertEquals(1, detail.groups().size());

    StudentView student = query.getStudentByJmbag("0036501001").orElseThrow();
    assertEquals("Ana Studentica", student.fullName());
    assertEquals(1, query.listStudents().size());

    assertEquals(1, query.listRooms().size());

    EnrollmentView enrollmentView = query.listEnrollments(courseId).get(0);
    assertEquals("0036501001", enrollmentView.studentJmbag());
    assertEquals("Ana Studentica", enrollmentView.studentFullName());

    // Admin console: list all users with their roles.
    List<AppUserView> appUsers = query.listUsers();
    assertEquals(2, appUsers.size());
    AppUserView lecturer =
        appUsers.stream()
            .filter(u -> u.username().equals("lecturer.marko"))
            .findFirst()
            .orElseThrow();
    assertTrue(lecturer.roles().contains("NOSITELJ"));
    assertTrue(lecturer.active());

    // Sync status snapshot: one of each provisioned above.
    SyncStatusView sync = query.syncStatus();
    assertEquals(1, sync.semesters());
    assertEquals(1, sync.courses());
    assertEquals(1, sync.students());
    assertEquals(1, sync.rooms());
  }

  @Test
  void scopesCourseListingToEnrolmentTeachingAndGlobalRole() {
    long attended = provisioning.provisionCourse("PROG", "Programiranje", "2024Z", 6, "o", "l");
    long taught = provisioning.provisionCourse("ALG", "Algoritmi", "2024Z", 5, "o", "l");
    long unrelated = provisioning.provisionCourse("FIZ", "Fizika", "2024Z", 4, "o", "l");

    long lecturerId =
        provisioning.provisionStaffUser(
            "lecturer.marko", "h", "Marko P", "m@fer.hr", Set.of("NOSITELJ"), LocalDateTime.now());
    provisioning.assignStaff(taught, lecturerId, "NOSITELJ");

    long studentId =
        provisioning.provisionStudent(
            "0036501010", "h", "Ana A", "ana@fer.hr", "0036501010", "R", 1, LocalDateTime.now());
    provisioning.enroll(studentId, attended, LocalDateTime.now());

    // Student sees only the course they are enrolled in.
    List<CourseSummaryView> studentCourses =
        query.listCoursesForPrincipal("0036501010", Set.of("STUDENT"), null);
    assertEquals(1, studentCourses.size());
    assertEquals("PROG", studentCourses.get(0).code());

    // Teaching staff see only the course they teach.
    List<CourseSummaryView> staffCourses =
        query.listCoursesForPrincipal("lecturer.marko", Set.of("NOSITELJ"), null);
    assertEquals(1, staffCourses.size());
    assertEquals("ALG", staffCourses.get(0).code());

    // Global roles see every course; the unrelated one is only visible to them.
    List<CourseSummaryView> adminCourses =
        query.listCoursesForPrincipal("admin", Set.of("ADMIN"), null);
    assertEquals(3, adminCourses.size());
    assertTrue(adminCourses.stream().anyMatch(c -> c.id() == unrelated));

    // Unknown principal sees nothing.
    assertTrue(query.listCoursesForPrincipal("nobody", Set.of("STUDENT"), null).isEmpty());
  }

  @Test
  void assignsStaffByUsernameAndReportsMissingUser() {
    long courseId =
        provisioning.provisionCourse("RASPORED", "Raspoređivanje", "2024Z", 5, "o", "l");
    long userId =
        provisioning.provisionStaffUser(
            "asistent.iva",
            "hash",
            "Iva Asistent",
            "iva@fer.hr",
            Set.of("ASISTENT"),
            LocalDateTime.now());
    assertTrue(userId > 0);

    assertTrue(provisioning.assignStaffByUsername(courseId, "asistent.iva", "ASISTENT"));
    CourseDetailView detail = query.courseDetail(courseId).orElseThrow();
    assertTrue(detail.staff().stream().anyMatch(s -> s.fullName().equals("Iva Asistent")));

    assertTrue(!provisioning.assignStaffByUsername(courseId, "ne.postoji", "ASISTENT"));
  }

  @Test
  void assignsStudentToGroupByJmbagAndExposesGroupCodes() {
    long courseId = provisioning.provisionCourse("PROG", "Programiranje", "2024Z", 6, "o", "l");
    long groupId = provisioning.provisionGroup(courseId, "L01", "LAB", "Pon", 16);
    long otherCourse = provisioning.provisionCourse("MAT", "Matematika", "2024Z", 5, "o", "l");
    long studentId =
        provisioning.provisionStudent(
            "0036509999", "h", "Ana A", "ana@fer.hr", "0036509999", "R", 1, LocalDateTime.now());
    provisioning.enroll(studentId, courseId, LocalDateTime.now());

    // Unknown student / group outside the course are rejected.
    assertTrue(!provisioning.assignStudentToGroup(courseId, "0000000000", groupId));
    assertTrue(!provisioning.assignStudentToGroup(courseId, "0036509999", 99999L));
    // Student not enrolled in the other course → rejected.
    assertTrue(!provisioning.assignStudentToGroup(otherCourse, "0036509999", groupId));

    assertTrue(provisioning.assignStudentToGroup(courseId, "0036509999", groupId));
    // Idempotent.
    assertTrue(provisioning.assignStudentToGroup(courseId, "0036509999", groupId));

    EnrollmentView view = query.listEnrollments(courseId).get(0);
    assertEquals(List.of("L01"), view.groupCodes());
  }

  @Test
  void provisioningIsIdempotent() {
    long firstCourse =
        provisioning.provisionCourse("UURA", "Uvod u računarstvo", "2024Z", 6, "o", "l");
    long secondCourse =
        provisioning.provisionCourse("UURA", "Uvod u računarstvo", "2024Z", 6, "o", "l");
    assertEquals(firstCourse, secondCourse);

    long firstRoom = provisioning.provisionRoom("A101", "A", 100, 2, false);
    long secondRoom = provisioning.provisionRoom("A101", "A", 100, 2, false);
    assertEquals(firstRoom, secondRoom);

    long firstStudent =
        provisioning.provisionStudent(
            "0036501002", "h", "B C", "b@fer.hr", "0036501002", "R", 1, LocalDateTime.now());
    long secondStudent =
        provisioning.provisionStudent(
            "0036501002", "h", "B C", "b@fer.hr", "0036501002", "R", 1, LocalDateTime.now());
    assertEquals(firstStudent, secondStudent);

    long firstEnrollment = provisioning.enroll(firstStudent, firstCourse, LocalDateTime.now());
    long secondEnrollment = provisioning.enroll(firstStudent, firstCourse, LocalDateTime.now());
    assertEquals(firstEnrollment, secondEnrollment);

    // Re-running timetable seeding must not duplicate the slot.
    long firstSlot =
        provisioning.provisionClassSchedule(
            firstCourse,
            null,
            "LECTURE",
            firstRoom,
            "MONDAY",
            java.time.LocalTime.of(10, 0),
            java.time.LocalTime.of(12, 0),
            "Prof");
    long secondSlot =
        provisioning.provisionClassSchedule(
            firstCourse,
            null,
            "LECTURE",
            firstRoom,
            "MONDAY",
            java.time.LocalTime.of(10, 0),
            java.time.LocalTime.of(12, 0),
            "Prof");
    assertEquals(firstSlot, secondSlot);
  }
}
