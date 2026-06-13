package hr.fer.zemris.ferko.webapi.bootstrap;

import hr.fer.zemris.ferko.application.usecase.academic.AcademicProvisioningService;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.CourseCatalogEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.EnrollmentEntry;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds real academic data (courses, staff, groups, students, enrollments) into the normalized
 * tables from the packaged legacy FER datasets. Idempotent and disabled in hardened profiles.
 */
@Component
@Order(6)
@ConditionalOnProperty(
    name = "ferko.seed.academic.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class AcademicDataSeeder implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(AcademicDataSeeder.class);
  private static final Pattern FIRST_INT = Pattern.compile("(\\d+)");
  private static final String SEMESTER_CODE = "2025Z";
  private static final String DEFAULT_PASSWORD = "ferko123";

  private final LegacyDatasetLoader datasetLoader;
  private final AcademicProvisioningService provisioning;
  private final PasswordEncoder passwordEncoder;
  private final int maxCourses;
  private final int maxStudents;

  public AcademicDataSeeder(
      LegacyDatasetLoader datasetLoader,
      AcademicProvisioningService provisioning,
      PasswordEncoder passwordEncoder,
      @Value("${ferko.seed.academic.max-courses:12}") int maxCourses,
      @Value("${ferko.seed.academic.max-students:120}") int maxStudents) {
    this.datasetLoader = datasetLoader;
    this.provisioning = provisioning;
    this.passwordEncoder = passwordEncoder;
    this.maxCourses = maxCourses;
    this.maxStudents = maxStudents;
  }

  @Override
  public void run(ApplicationArguments args) {
    LegacyDataset dataset = datasetLoader.load();
    LocalDateTime now = LocalDateTime.now();
    String studentPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

    provisioning.provisionSemester(
        SEMESTER_CODE,
        "2025/2026",
        "ZIMSKI",
        LocalDate.of(2025, 10, 1),
        LocalDate.of(2026, 2, 15),
        true);

    seedRooms();

    long lecturerId = ensureStaff("lecturer.marko", "Marko Predavač", "NOSITELJ", now);
    long assistantId = ensureStaff("assistant.iva", "Iva Asistent", "ASISTENT", now);

    // Pick the courses that actually have enrolled students (ranked by enrollment count) so the
    // seeded portal is non-empty and the scheduler runs on real cohorts.
    Map<String, Integer> enrollmentCountByCode = new LinkedHashMap<>();
    Map<String, String> courseNameByCode = new LinkedHashMap<>();
    for (EnrollmentEntry enrollment : dataset.enrollments()) {
      String code = enrollment.courseCode();
      if (code == null || code.isBlank()) {
        continue;
      }
      enrollmentCountByCode.merge(code, 1, Integer::sum);
      courseNameByCode.putIfAbsent(code, enrollment.courseName());
    }
    List<String> selectedCodes =
        enrollmentCountByCode.entrySet().stream()
            .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
            .limit(maxCourses)
            .map(Map.Entry::getKey)
            .toList();

    Map<String, Long> courseIdByCode = new LinkedHashMap<>();
    Map<String, Long> labGroupByCourse = new LinkedHashMap<>();
    int scheduleIndex = 0;
    for (String code : selectedCodes) {
      CourseCatalogEntry entry = dataset.courses().get(code);
      // Prefer the human-readable course name from enrollment data (e.g. "Fizika 2") over the
      // ISVU catalogue placeholder, then the catalogue title, then the bare code.
      String name =
          blankToDefault(
              courseNameByCode.get(code),
              entry != null ? blankToDefault(entry.title(), code) : code);
      long courseId =
          provisioning.provisionCourse(
              code,
              name,
              SEMESTER_CODE,
              entry != null ? parseEcts(entry.workloadRaw()) : 5,
              entry != null ? entry.description() : null,
              entry != null ? entry.literature() : null);
      provisioning.assignStaff(courseId, lecturerId, "NOSITELJ");
      provisioning.assignStaff(courseId, assistantId, "ASISTENT");
      provisioning.provisionGroup(courseId, "P1", "LECTURE", "Predavanja", 400);
      long labGroup = provisioning.provisionGroup(courseId, "L1", "LAB", "Laboratorij", 400);
      courseIdByCode.put(code, courseId);
      labGroupByCourse.put(code, labGroup);
      seedWeeklySchedule(courseId, labGroup, scheduleIndex++);
    }

    Map<String, Long> studentIdByJmbag = new LinkedHashMap<>();
    for (EnrollmentEntry enrollment : dataset.enrollments()) {
      String jmbag = enrollment.jmbag();
      Long courseId = courseIdByCode.get(enrollment.courseCode());
      if (jmbag == null || jmbag.isBlank() || courseId == null) {
        continue;
      }
      if (!studentIdByJmbag.containsKey(jmbag) && studentIdByJmbag.size() >= maxStudents) {
        continue;
      }
      long studentId =
          studentIdByJmbag.computeIfAbsent(
              jmbag,
              key ->
                  provisioning.provisionStudent(
                      key,
                      studentPassword,
                      blankToDefault(enrollment.studentName(), "Student " + key),
                      key + "@fer.hr",
                      key,
                      "Računarstvo",
                      Math.max(1, enrollment.yearOfStudy()),
                      now));
      long enrollmentId = provisioning.enroll(studentId, courseId, now);
      Long labGroup = labGroupByCourse.get(enrollment.courseCode());
      if (labGroup != null) {
        provisioning.assignGroup(enrollmentId, labGroup);
      }
    }

    // Make the demo STUDENT user (student.ana, created by the user seeder) a real enrolled student
    // so every student-facing feature (burza grupa, kalendar, ankete) works out of the box.
    if (!courseIdByCode.isEmpty()) {
      long demoCourseId = courseIdByCode.values().iterator().next();
      long demoStudentId =
          provisioning.provisionStudent(
              "student.ana",
              studentPassword,
              "Ana Studentica",
              "ana@fer.hr",
              "0036500000",
              "Računarstvo",
              1,
              now);
      provisioning.enroll(demoStudentId, demoCourseId, now);
    }

    LOG.info(
        "Academic seeding complete: {} courses, {} students.",
        courseIdByCode.size(),
        studentIdByJmbag.size());
  }

  /**
   * Seeds a deterministic weekly lecture + lab slot for a course so the calendar is non-empty. The
   * day/time is varied by {@code index} to spread courses across the week.
   */
  private void seedWeeklySchedule(long courseId, long labGroupId, int index) {
    String lectureDay = DayOfWeek.of((index % 5) + 1).name();
    String labDay = DayOfWeek.of(((index + 2) % 5) + 1).name();
    int lectureHour = 8 + (index % 6); // 08:00..13:00
    provisioning.provisionClassSchedule(
        courseId,
        null,
        "LECTURE",
        null,
        lectureDay,
        LocalTime.of(lectureHour, 0),
        LocalTime.of(lectureHour + 2, 0),
        "Marko Predavač");
    provisioning.provisionClassSchedule(
        courseId,
        labGroupId,
        "LAB",
        null,
        labDay,
        LocalTime.of(14, 0),
        LocalTime.of(16, 0),
        "Iva Asistent");
  }

  private void seedRooms() {
    provisioning.provisionRoom("A101", "Sivi (A)", 130, 3, false);
    provisioning.provisionRoom("A201", "Sivi (A)", 90, 2, false);
    provisioning.provisionRoom("B1", "Bijeli (B)", 60, 2, false);
    provisioning.provisionRoom("D1", "Dordežićeva (D)", 250, 5, false);
    provisioning.provisionRoom("Siv. predavaonica", "Sivi (A)", 300, 6, false);
    provisioning.provisionRoom("Računalni praktikum 1", "Bijeli (B)", 30, 1, true);
    provisioning.provisionRoom("Računalni praktikum 2", "Bijeli (B)", 30, 1, true);
  }

  private long ensureStaff(String username, String fullName, String role, LocalDateTime now) {
    return provisioning.provisionStaffUser(
        username,
        passwordEncoder.encode(DEFAULT_PASSWORD),
        fullName,
        username + "@fer.hr",
        Set.of(role),
        now);
  }

  private static String blankToDefault(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }

  private static int parseEcts(String workloadRaw) {
    if (workloadRaw == null) {
      return 5;
    }
    Matcher matcher = FIRST_INT.matcher(workloadRaw);
    if (matcher.find()) {
      int value = Integer.parseInt(matcher.group(1));
      return value >= 1 && value <= 30 ? value : 5;
    }
    return 5;
  }
}
