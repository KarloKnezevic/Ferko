package hr.fer.zemris.ferko.application.usecase.academic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.support.InMemoryAcademicRepositories;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AcademicServicesTest {

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
        new AcademicProvisioningService(semesters, courses, enrollments, students, rooms, users);
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
  }
}
