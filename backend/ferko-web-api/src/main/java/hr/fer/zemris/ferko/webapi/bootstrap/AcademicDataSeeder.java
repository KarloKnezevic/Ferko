package hr.fer.zemris.ferko.webapi.bootstrap;

import hr.fer.zemris.ferko.application.usecase.academic.AcademicProvisioningService;
import hr.fer.zemris.ferko.application.usecase.demonstrator.DemonstratorService;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.CourseCatalogEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.EnrollmentEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.ScheduleEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.RoomInference.RoomSpec;
import hr.fer.zemris.ferko.webapi.config.FerkoProperties;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds real academic data (courses, staff, groups, students, enrollments, rooms) into the
 * normalized tables from the packaged legacy FER datasets. Idempotent and disabled in hardened
 * profiles.
 *
 * <p>The demo presents an active <strong>Summer 2026</strong> semester (where the full dataset is
 * loaded) and keeps a past <strong>Winter 2025/2026</strong> semester for history. Scale is
 * controlled by {@link FerkoProperties} ({@code ferko.seed.academic.*}); a limit of {@code 0} or
 * below means "no limit — seed the entire bundled dataset".
 */
@Component
@Order(6)
@ConditionalOnProperty(
    name = "ferko.seed.academic.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class AcademicDataSeeder implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(AcademicDataSeeder.class);
  private static final String DEFAULT_PASSWORD = "ferko123";

  /** Active semester: legacy dates are mapped onto the Summer 2026 term. */
  private static final String SUMMER_CODE = "2026LJ";

  /** Past semester kept on record so the semester list shows a completed prior term. */
  private static final String WINTER_CODE = "2025Z";

  private final LegacyDatasetLoader datasetLoader;
  private final AcademicProvisioningService provisioning;
  private final DemonstratorService demonstratorService;
  private final PasswordEncoder passwordEncoder;
  private final int maxCourses;
  private final int maxStudents;

  // Maps a professor display name to the (disambiguated) username assigned to them, so two distinct
  // professors never collapse onto a single seeded user.
  private final Map<String, String> usernameByStaffName = new LinkedHashMap<>();
  private final Set<String> usedStaffUsernames = new LinkedHashSet<>();

  public AcademicDataSeeder(
      LegacyDatasetLoader datasetLoader,
      AcademicProvisioningService provisioning,
      DemonstratorService demonstratorService,
      PasswordEncoder passwordEncoder,
      FerkoProperties properties) {
    this.datasetLoader = datasetLoader;
    this.provisioning = provisioning;
    this.demonstratorService = demonstratorService;
    this.passwordEncoder = passwordEncoder;
    this.maxCourses = properties.getSeed().getAcademic().getMaxCourses();
    this.maxStudents = properties.getSeed().getAcademic().getMaxStudents();
  }

  @Override
  public void run(ApplicationArguments args) {
    LegacyDataset dataset = datasetLoader.load();
    LocalDateTime now = LocalDateTime.now();
    String studentPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

    // Past winter and active summer semester. The summer term carries the full dataset.
    provisioning.provisionSemester(
        WINTER_CODE,
        "2025/2026",
        "ZIMSKI",
        LocalDate.of(2025, 10, 1),
        LocalDate.of(2026, 2, 14),
        false);
    provisioning.provisionSemester(
        SUMMER_CODE,
        "2025/2026",
        "LJETNI",
        LocalDate.of(2026, 3, 2),
        LocalDate.of(2026, 7, 3),
        true);

    seedRooms(dataset);

    // Demo teaching accounts retained so the seeded logins always have something to manage.
    long demoLecturerId = ensureStaff("lecturer.marko", "Marko Predavač", "NOSITELJ", now);
    long demoAssistantId = ensureStaff("assistant.iva", "Iva Asistent", "ASISTENT", now);

    List<String> selectedCodes = selectCourses(dataset);
    Map<String, String> courseNameByCode = courseNamesByCode(dataset);
    Map<String, List<ScheduleEntry>> schedulesByCode =
        dataset.schedules().stream()
            .filter(s -> s.courseCode() != null && !s.courseCode().isBlank())
            .collect(Collectors.groupingBy(ScheduleEntry::courseCode));

    Map<String, Long> courseIdByCode = new LinkedHashMap<>();
    Map<String, Long> labGroupByCourse = new LinkedHashMap<>();
    int scheduleIndex = 0;
    for (String code : selectedCodes) {
      CourseCatalogEntry entry = dataset.courses().get(code);
      CourseWorkload workload = CourseWorkload.parse(entry != null ? entry.workloadRaw() : null, 5);
      long courseId =
          provisioning.provisionCourse(
              code,
              courseName(courseNameByCode, entry, code),
              SUMMER_CODE,
              workload.ects(),
              description(entry, workload),
              entry != null ? entry.literature() : null);
      assignCourseStaff(courseId, entry, demoLecturerId, demoAssistantId, now);
      provisioning.provisionGroup(courseId, "P1", "LECTURE", "Predavanja", 400);
      long labGroup = provisioning.provisionGroup(courseId, "L1", "LAB", "Laboratorij", 400);
      courseIdByCode.put(code, courseId);
      labGroupByCourse.put(code, labGroup);
      seedTimetable(
          courseId, labGroup, schedulesByCode.get(code), courseHolder(entry), scheduleIndex++);
    }

    Map<String, Long> studentIdByJmbag =
        seedStudents(dataset, courseIdByCode, labGroupByCourse, studentPassword, now);
    long demoStudentId = seedDemoStudentEnrollment(courseIdByCode, studentPassword, now);
    int demonstrators = seedDemonstrators(courseIdByCode, studentIdByJmbag, demoStudentId);

    LOG.info(
        "Academic seeding complete: {} courses, {} students, {} demonstrator assignments.",
        courseIdByCode.size(),
        studentIdByJmbag.size(),
        demonstrators);
  }

  /**
   * Assigns demonstrators from the legacy lab files: a demonstrator JMBAG becomes a demonstrator on
   * a seeded course when that student is also seeded. The demo student is made a demonstrator on
   * their course so the "my demonstratures" view is populated.
   */
  private int seedDemonstrators(
      Map<String, Long> courseIdByCode, Map<String, Long> studentIdByJmbag, long demoStudentId) {
    Map<String, Set<String>> demonstratorJmbags = datasetLoader.loadDemonstratorJmbagsByCourse();
    int assignments = 0;
    for (Map.Entry<String, Long> course : courseIdByCode.entrySet()) {
      Set<String> jmbags = demonstratorJmbags.get(course.getKey());
      if (jmbags == null) {
        continue;
      }
      for (String jmbag : jmbags) {
        Long studentId = studentIdByJmbag.get(jmbag);
        if (studentId != null) {
          demonstratorService.assign(course.getValue(), studentId);
          assignments++;
        }
      }
    }
    if (demoStudentId > 0 && !courseIdByCode.isEmpty()) {
      demonstratorService.assign(courseIdByCode.values().iterator().next(), demoStudentId);
      assignments++;
    }
    return assignments;
  }

  /** Selects the courses with the most enrolled students; an unlimited cap seeds them all. */
  private List<String> selectCourses(LegacyDataset dataset) {
    Map<String, Integer> enrollmentCountByCode = new LinkedHashMap<>();
    for (EnrollmentEntry enrollment : dataset.enrollments()) {
      String code = enrollment.courseCode();
      if (code != null && !code.isBlank()) {
        enrollmentCountByCode.merge(code, 1, Integer::sum);
      }
    }
    var ranked =
        enrollmentCountByCode.entrySet().stream()
            .sorted((left, right) -> Integer.compare(right.getValue(), left.getValue()))
            .map(Map.Entry::getKey);
    if (maxCourses > 0) {
      ranked = ranked.limit(maxCourses);
    }
    return ranked.toList();
  }

  /**
   * Seeds students and their enrollments into the active semester's courses, returning jmbag→id.
   */
  private Map<String, Long> seedStudents(
      LegacyDataset dataset,
      Map<String, Long> courseIdByCode,
      Map<String, Long> labGroupByCourse,
      String studentPassword,
      LocalDateTime now) {
    Map<String, Long> studentIdByJmbag = new LinkedHashMap<>();
    for (EnrollmentEntry enrollment : dataset.enrollments()) {
      String jmbag = enrollment.jmbag();
      Long courseId = courseIdByCode.get(enrollment.courseCode());
      if (jmbag == null || jmbag.isBlank() || courseId == null) {
        continue;
      }
      if (!studentIdByJmbag.containsKey(jmbag)
          && maxStudents > 0
          && studentIdByJmbag.size() >= maxStudents) {
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
    return studentIdByJmbag;
  }

  /**
   * Makes the demo STUDENT user (student.ana) a real enrolled student in the active semester so the
   * student-facing features (calendar, group exchange, surveys) work out of the box. Returns the
   * demo student's id, or {@code 0} when there are no courses to enroll into.
   */
  private long seedDemoStudentEnrollment(
      Map<String, Long> courseIdByCode, String studentPassword, LocalDateTime now) {
    if (courseIdByCode.isEmpty()) {
      return 0L;
    }
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
    long currentCourseId = courseIdByCode.values().iterator().next();
    provisioning.enroll(demoStudentId, currentCourseId, now);
    return demoStudentId;
  }

  /** Assigns the real course holders/lecturers plus the demo teaching accounts. */
  private void assignCourseStaff(
      long courseId,
      CourseCatalogEntry entry,
      long demoLecturerId,
      long demoAssistantId,
      LocalDateTime now) {
    provisioning.assignStaff(courseId, demoLecturerId, "NOSITELJ");
    provisioning.assignStaff(courseId, demoAssistantId, "ASISTENT");
    if (entry == null) {
      return;
    }
    for (String leader : StaffNames.parseNames(entry.leaders())) {
      Long id = ensureNamedStaff(leader, "NOSITELJ", now);
      if (id != null) {
        provisioning.assignStaff(courseId, id, "NOSITELJ");
      }
    }
    for (String instructor : StaffNames.parseNames(entry.instructors())) {
      Long id = ensureNamedStaff(instructor, "NASTAVNIK", now);
      if (id != null) {
        provisioning.assignStaff(courseId, id, "NASTAVNIK");
      }
    }
  }

  /**
   * Provisions (idempotently) a staff user for a real professor name; null if the name is unusable.
   * Distinct professors whose names map to the same base username get a numeric suffix so they stay
   * separate users.
   */
  private Long ensureNamedStaff(String displayName, String role, LocalDateTime now) {
    String username = usernameByStaffName.get(displayName);
    if (username == null) {
      String base = StaffNames.toUsername(displayName);
      if (base.isBlank()) {
        return null;
      }
      username = base;
      for (int suffix = 2; usedStaffUsernames.contains(username); suffix++) {
        username = base + suffix;
      }
      usedStaffUsernames.add(username);
      usernameByStaffName.put(displayName, username);
    }
    return ensureStaff(username, displayName, role, now);
  }

  /** Derives the room catalogue from the timetable's distinct room codes. */
  private void seedRooms(LegacyDataset dataset) {
    Set<String> roomCodes = new LinkedHashSet<>();
    for (ScheduleEntry schedule : dataset.schedules()) {
      String room = schedule.room();
      if (room != null && !room.isBlank()) {
        roomCodes.add(room.trim());
      }
    }
    if (roomCodes.isEmpty()) {
      // Defensive fallback so exam seating always has somewhere to place students.
      provisioning.provisionRoom("A101", "Siva zgrada (A)", 120, 3, false);
      provisioning.provisionRoom("PCLAB1", "Bijela zgrada (B)", 20, 1, true);
      return;
    }
    for (String code : roomCodes) {
      RoomSpec spec = RoomInference.infer(code);
      provisioning.provisionRoom(
          code, spec.building(), spec.capacity(), spec.requiredAssistants(), spec.hasComputers());
    }
    LOG.info("Seeded {} rooms derived from the legacy timetable.", roomCodes.size());
  }

  /**
   * Imports the real weekly timetable for a course from the legacy {@code satnica} entries
   * (distinct weekday/time/room slots, dates mapped to weekday), falling back to a deterministic
   * synthetic slot when the course has no timetable data so the calendar is never empty.
   */
  private void seedTimetable(
      long courseId, long labGroupId, List<ScheduleEntry> schedules, String instructor, int index) {
    if (schedules == null || schedules.isEmpty()) {
      seedWeeklySchedule(courseId, labGroupId, index);
      return;
    }
    Set<String> seen = new LinkedHashSet<>();
    for (ScheduleEntry schedule : schedules) {
      if (schedule.date() == null || schedule.startsAt() == null) {
        continue;
      }
      DayOfWeek day = schedule.date().getDayOfWeek();
      LocalTime start = schedule.startsAt();
      LocalTime end = start.plus(Duration.ofMinutes(Math.max(60, schedule.durationMinutes())));
      String room = schedule.room() == null ? "" : schedule.room().trim();
      String key = day + "|" + start + "|" + end + "|" + room;
      if (!seen.add(key)) {
        continue;
      }
      Long roomId = room.isBlank() ? null : ensureRoom(room);
      provisioning.provisionClassSchedule(
          courseId, null, "LECTURE", roomId, day.name(), start, end, instructor);
    }
  }

  /** Resolves (idempotently) a room id from a code, inferring its attributes if new. */
  private Long ensureRoom(String code) {
    RoomSpec spec = RoomInference.infer(code);
    return provisioning.provisionRoom(
        code, spec.building(), spec.capacity(), spec.requiredAssistants(), spec.hasComputers());
  }

  /** Course holder display name for the timetable instructor field. */
  private static String courseHolder(CourseCatalogEntry entry) {
    if (entry == null) {
      return "";
    }
    List<String> leaders = StaffNames.parseNames(entry.leaders());
    return leaders.isEmpty() ? "" : leaders.get(0);
  }

  /**
   * Seeds a deterministic weekly lecture + lab slot for a course so the calendar is non-empty. The
   * day/time is varied by {@code index} to spread courses across the week. Used only as a fallback
   * when the course has no real timetable data.
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

  private long ensureStaff(String username, String fullName, String role, LocalDateTime now) {
    return provisioning.provisionStaffUser(
        username,
        passwordEncoder.encode(DEFAULT_PASSWORD),
        fullName,
        username + "@fer.hr",
        Set.of(role),
        now);
  }

  /** Builds, in a single pass, the first human-readable course name seen per course code. */
  private static Map<String, String> courseNamesByCode(LegacyDataset dataset) {
    Map<String, String> names = new LinkedHashMap<>();
    for (EnrollmentEntry enrollment : dataset.enrollments()) {
      String code = enrollment.courseCode();
      if (code != null
          && !code.isBlank()
          && !blankToDefault(enrollment.courseName(), "").isBlank()) {
        names.putIfAbsent(code, enrollment.courseName());
      }
    }
    return names;
  }

  /** Course name: prefer the human-readable enrollment name, then catalogue title, then code. */
  private static String courseName(
      Map<String, String> courseNameByCode, CourseCatalogEntry entry, String code) {
    String fromEnrollment = courseNameByCode.get(code);
    if (fromEnrollment != null && !fromEnrollment.isBlank()) {
      return fromEnrollment;
    }
    return entry != null ? blankToDefault(entry.title(), code) : code;
  }

  private static String description(CourseCatalogEntry entry, CourseWorkload workload) {
    String base = entry != null ? blankToDefault(entry.description(), "") : "";
    String hours = workload.hoursSummary();
    if (hours.isEmpty()) {
      return base.isBlank() ? null : base;
    }
    String workloadLine = "Opterećenje (sati/tjedan): " + hours + " · ECTS: " + workload.ects();
    return base.isBlank() ? workloadLine : base + "\n\n" + workloadLine;
  }

  private static String blankToDefault(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value;
  }
}
