package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Course;
import hr.fer.zemris.ferko.domain.model.CourseStaff;
import hr.fer.zemris.ferko.domain.model.Enrollment;
import hr.fer.zemris.ferko.domain.model.EnrollmentStatus;
import hr.fer.zemris.ferko.domain.model.GroupMembership;
import hr.fer.zemris.ferko.domain.model.GroupType;
import hr.fer.zemris.ferko.domain.model.Role;
import hr.fer.zemris.ferko.domain.model.Room;
import hr.fer.zemris.ferko.domain.model.Semester;
import hr.fer.zemris.ferko.domain.model.Student;
import hr.fer.zemris.ferko.domain.model.StudentGroup;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class AcademicJdbcRepositoriesTest {

  private JdbcTemplate jdbcTemplate;

  @BeforeEach
  void setUp() throws SQLException {
    String databaseName = "academic_" + UUID.randomUUID().toString().replace("-", "");
    String url =
        "jdbc:h2:mem:" + databaseName + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE";
    DriverManagerDataSource dataSource = new DriverManagerDataSource(url, "sa", "");
    jdbcTemplate = new JdbcTemplate(dataSource);
    try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("academic-schema-h2.sql"));
    }
  }

  @Test
  void persistsUsersWithRolesAndUpdatesThem() {
    JdbcAppUserRepository users = new JdbcAppUserRepository(jdbcTemplate);

    AppUser saved =
        users.save(
            new AppUser(
                0L,
                "admin.ferko",
                "hash",
                "Admin Ferko",
                "admin@fer.hr",
                true,
                LocalDateTime.parse("2026-01-01T08:00:00"),
                EnumSet.of(Role.ADMIN, Role.NOSITELJ)));

    assertTrue(saved.id() > 0);
    assertEquals(2, saved.roles().size());
    assertTrue(users.findByUsername("admin.ferko").isPresent());
    assertEquals(1, users.findAll().size());

    AppUser updated =
        users.save(
            new AppUser(
                saved.id(),
                "admin.ferko",
                "hash2",
                "Admin Ferko",
                "admin@fer.hr",
                false,
                saved.createdAt(),
                EnumSet.of(Role.ADMIN)));
    assertFalse(updated.active());
    assertEquals(1, updated.roles().size());
    assertEquals("hash2", users.findById(saved.id()).orElseThrow().passwordHash());
  }

  @Test
  void upsertsSemestersAndFindsActive() {
    JdbcSemesterRepository semesters = new JdbcSemesterRepository(jdbcTemplate);
    semesters.save(
        new Semester(
            "2024Z",
            "2024/2025",
            "ZIMSKI",
            LocalDate.parse("2024-10-01"),
            LocalDate.parse("2025-02-15"),
            true));
    semesters.save(
        new Semester(
            "2024L",
            "2024/2025",
            "LJETNI",
            LocalDate.parse("2025-03-01"),
            LocalDate.parse("2025-07-15"),
            false));

    assertEquals(2, semesters.findAll().size());
    assertEquals("2024Z", semesters.findActive().orElseThrow().code());

    // upsert: deactivate 2024Z
    semesters.save(
        new Semester(
            "2024Z",
            "2024/2025",
            "ZIMSKI",
            LocalDate.parse("2024-10-01"),
            LocalDate.parse("2025-02-15"),
            false));
    assertTrue(semesters.findActive().isEmpty());
    assertEquals(2, semesters.findAll().size());
  }

  @Test
  void persistsRooms() {
    JdbcRoomRepository rooms = new JdbcRoomRepository(jdbcTemplate);
    Room saved = rooms.save(new Room(0L, "A101", "Zgrada A", 120, 3, false));
    assertTrue(saved.id() > 0);
    rooms.save(new Room(saved.id(), "A101", "Zgrada A", 150, 4, true));
    Room reloaded = rooms.findByCode("A101").orElseThrow();
    assertEquals(150, reloaded.capacity());
    assertTrue(reloaded.hasComputers());
    assertEquals(1, rooms.findAll().size());
  }

  @Test
  void persistsCoursesWithStaffAndGroups() {
    JdbcSemesterRepository semesters = new JdbcSemesterRepository(jdbcTemplate);
    semesters.save(
        new Semester(
            "2024Z",
            "2024/2025",
            "ZIMSKI",
            LocalDate.parse("2024-10-01"),
            LocalDate.parse("2025-02-15"),
            true));

    JdbcCourseRepository courses = new JdbcCourseRepository(jdbcTemplate);
    Course course =
        courses.save(new Course(0L, "UURA", "Uvod u računarstvo", "2024Z", 6, "opis", "lit"));
    assertTrue(course.id() > 0);
    assertEquals("UURA", courses.findByCodeAndSemester("UURA", "2024Z").orElseThrow().code());
    assertEquals(1, courses.findBySemester("2024Z").size());

    courses.addStaff(new CourseStaff(0L, course.id(), 2L, Role.NOSITELJ));
    courses.addStaff(new CourseStaff(0L, course.id(), 3L, Role.ASISTENT));
    assertEquals(2, courses.findStaffByCourse(course.id()).size());

    courses.addGroup(new StudentGroup(0L, course.id(), "P01", GroupType.LECTURE, "Pon", 200));
    StudentGroup lab =
        courses.addGroup(new StudentGroup(0L, course.id(), "L01", GroupType.LAB, "Uto", 16));
    assertTrue(lab.id() > 0);
    assertEquals(2, courses.findGroupsByCourse(course.id()).size());

    courses.save(new Course(course.id(), "UURA", "Uvod u računarstvo", "2024Z", 7, "opis2", "lit"));
    assertEquals(7, courses.findById(course.id()).orElseThrow().ects());
  }

  @Test
  void persistsStudentsEnrollmentsAndMemberships() {
    JdbcStudentRepository students = new JdbcStudentRepository(jdbcTemplate);
    Student student = students.save(new Student(0L, 42L, "0036000001", "Računarstvo", 1));
    assertTrue(student.id() > 0);
    assertEquals("0036000001", students.findByJmbag("0036000001").orElseThrow().jmbag());
    assertTrue(students.findByUserId(42L).isPresent());

    students.save(new Student(student.id(), 42L, "0036000001", "Računarstvo", 2));
    assertEquals(2, students.findById(student.id()).orElseThrow().yearOfStudy());
    assertEquals(1, students.findAll().size());

    JdbcEnrollmentRepository enrollments = new JdbcEnrollmentRepository(jdbcTemplate);
    Enrollment enrollment =
        enrollments.save(
            new Enrollment(0L, student.id(), 100L, LocalDateTime.now(), EnrollmentStatus.ACTIVE));
    assertTrue(enrollment.id() > 0);
    assertTrue(enrollments.findByStudentAndCourse(student.id(), 100L).isPresent());
    assertEquals(1, enrollments.findByCourse(100L).size());
    assertEquals(1, enrollments.findByStudent(student.id()).size());

    GroupMembership membership =
        enrollments.assignGroup(new GroupMembership(0L, enrollment.id(), 7L));
    assertTrue(membership.id() > 0);
    assertEquals(1, enrollments.findMembershipsByEnrollment(enrollment.id()).size());

    enrollments.save(
        new Enrollment(
            enrollment.id(), student.id(), 100L, LocalDateTime.now(), EnrollmentStatus.WITHDRAWN));
    assertEquals(
        EnrollmentStatus.WITHDRAWN, enrollments.findById(enrollment.id()).orElseThrow().status());
  }
}
