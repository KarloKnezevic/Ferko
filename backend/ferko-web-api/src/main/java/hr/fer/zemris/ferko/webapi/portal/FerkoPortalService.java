package hr.fer.zemris.ferko.webapi.portal;

import com.fasterxml.jackson.databind.JsonNode;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.CourseCatalogEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.EnrollmentEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.ExamEntry;
import hr.fer.zemris.ferko.webapi.bootstrap.LegacyDataset.ScheduleEntry;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FerkoPortalService {

  private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
  private static final String DEFAULT_PASSWORD = "ferko123";

  private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
  private final Map<String, PortalUser> usersByUsername = new LinkedHashMap<>();
  private final Map<Long, StudentProfile> studentsById = new LinkedHashMap<>();
  private final Map<String, Semester> semestersByCode = new LinkedHashMap<>();
  private final Map<String, Course> coursesByCode = new LinkedHashMap<>();
  private final Map<Long, ScheduleEvent> scheduleById = new LinkedHashMap<>();
  private final Map<Long, PointEntry> pointsById = new LinkedHashMap<>();
  private final Map<Long, ExamEvent> examsById = new LinkedHashMap<>();
  private final Map<Long, GroupExchangeRequest> groupExchangesById = new LinkedHashMap<>();
  private final Map<String, SyncOperation> syncOperations = new LinkedHashMap<>();
  private final Deque<ActivityLogEntry> activityLog = new ArrayDeque<>();

  private final AtomicLong userIdSequence = new AtomicLong(9300);
  private final AtomicLong scheduleIdSequence = new AtomicLong(1200);
  private final AtomicLong pointIdSequence = new AtomicLong(1600);
  private final AtomicLong examIdSequence = new AtomicLong(2000);
  private final AtomicLong exchangeIdSequence = new AtomicLong(2500);

  private String activeSemesterCode;

  public FerkoPortalService() {
    seedUsersAndStudents();
    seedAcademicStructure();
    seedSchedule();
    seedPoints();
    seedExams();
    seedGroupExchanges();
    seedSyncOperations();
    seedActivity();
  }

  public synchronized Optional<AuthView> authenticate(String username, String password) {
    if (isBlank(username) || isBlank(password)) {
      return Optional.empty();
    }
    PortalUser user = usersByUsername.get(username.trim().toLowerCase(Locale.ROOT));
    if (user == null || !passwordEncoder.matches(password, user.passwordHash)) {
      return Optional.empty();
    }
    return Optional.of(toAuthView(user));
  }

  public synchronized AuthView findUserById(long userId) {
    PortalUser user =
        usersByUsername.values().stream()
            .filter(candidate -> candidate.userId == userId)
            .findFirst()
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session."));
    return toAuthView(user);
  }

  public synchronized WorkspaceView buildWorkspace(PortalSessionUser sessionUser) {
    PortalRole role = sessionUser.role();

    List<CourseView> allCourses = coursesByCode.values().stream().map(this::toCourseView).toList();
    List<CourseView> myCourses = resolveMyCourses(sessionUser);
    List<StudentView> visibleStudents = resolveVisibleStudents(sessionUser);
    List<UserView> visibleUsers = resolveVisibleUsers(sessionUser);
    List<ScheduleView> visibleSchedule = resolveVisibleSchedule(sessionUser);
    List<ScheduleView> lectureSchedule =
        visibleSchedule.stream().filter(event -> "LECTURE".equals(event.type())).toList();
    List<ScheduleView> labSchedule =
        visibleSchedule.stream().filter(event -> "LAB".equals(event.type())).toList();
    List<PointView> visiblePoints = resolveVisiblePoints(sessionUser);

    List<GradeOverviewView> gradingOverview = buildGradeOverview(visiblePoints);
    List<ExamView> visibleExams = resolveVisibleExams(sessionUser);
    List<GroupExchangeView> visibleGroupExchanges = resolveVisibleGroupExchanges(sessionUser);

    List<NavigationItemView> navigation = navigationForRole(role);
    List<StatCardView> cards = buildCards(sessionUser, myCourses, visibleStudents, visibleExams);
    List<ActivityView> recentActivity =
        activityLog.stream().limit(15).map(ActivityLogEntry::toView).toList();

    return new WorkspaceView(
        "FERKO",
        "Faculty of Electrical Engineering and Computing, University of Zagreb",
        findUserById(sessionUser.userId()),
        navigation,
        cards,
        currentSemesterView(),
        semestersByCode.values().stream().map(Semester::toView).toList(),
        allCourses,
        myCourses,
        visibleStudents,
        visibleUsers,
        visibleSchedule,
        lectureSchedule,
        labSchedule,
        visiblePoints,
        gradingOverview,
        visibleExams,
        visibleGroupExchanges,
        buildPermissionsView(),
        syncOperations.values().stream().map(SyncOperation::toView).toList(),
        recentActivity,
        demoAccounts(),
        enabledActionsForRole(role));
  }

  public synchronized List<DemoAccountView> demoAccountsView() {
    return demoAccounts();
  }

  public synchronized ActionResult executeAction(
      PortalSessionUser actor, String actionType, JsonNode payload) {
    if (isBlank(actionType)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action type is required.");
    }

    String normalizedAction = actionType.trim().toUpperCase(Locale.ROOT);
    String message =
        switch (normalizedAction) {
          case "CREATE_SEMESTER" -> createSemester(actor, payload);
          case "ACTIVATE_SEMESTER" -> activateSemester(actor, payload);
          case "CREATE_COURSE" -> createCourse(actor, payload);
          case "ASSIGN_STAFF" -> assignStaff(actor, payload);
          case "DEFINE_GROUP" -> defineGroup(actor, payload);
          case "IMPORT_STUDENTS" -> importStudents(actor, payload);
          case "SYNC_STUDENTS" -> syncOperation(actor, "Student synchronization");
          case "SYNC_ROOMS" -> syncOperation(actor, "Room synchronization");
          case "SYNC_LECTURES" -> syncOperation(actor, "Lecture schedule synchronization");
          case "SYNC_LABS" -> syncOperation(actor, "Laboratory schedule synchronization");
          case "ENTER_POINTS" -> enterPoints(actor, payload);
          case "PUBLISH_POINTS" -> publishPoints(actor, payload);
          case "CREATE_EXAM" -> createExam(actor, payload);
          case "PUBLISH_EXAM_RESULTS" -> publishExam(actor, payload);
          case "REQUEST_GROUP_EXCHANGE" -> requestGroupExchange(actor, payload);
          case "DECIDE_GROUP_EXCHANGE" -> decideGroupExchange(actor, payload);
          case "ENROLL_STUDENT" -> enrollStudent(actor, payload);
          case "ASSIGN_STUDENT_GROUP" -> assignStudentGroup(actor, payload);
          default ->
              throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported action.");
        };

    return new ActionResult(message, buildWorkspace(actor));
  }

  private String createSemester(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.ADMIN);
    String code = requiredText(payload, "code").toUpperCase(Locale.ROOT);
    if (semestersByCode.containsKey(code)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semester already exists.");
    }

    Semester semester =
        new Semester(
            code,
            requiredText(payload, "academicYear"),
            requiredText(payload, "term"),
            requiredDate(payload, "startDate"),
            requiredDate(payload, "endDate"),
            false);
    semestersByCode.put(semester.code, semester);

    logActivity(
        actor, "Create semester", code + " / " + semester.academicYear + " " + semester.term);
    return "Semester " + code + " created.";
  }

  private String activateSemester(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.ADMIN);
    String code = requiredText(payload, "code").toUpperCase(Locale.ROOT);
    Semester semester =
        Optional.ofNullable(semestersByCode.get(code))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Semester not found."));

    semestersByCode.replaceAll((key, value) -> value.withActive(Objects.equals(key, code)));
    activeSemesterCode = code;

    logActivity(actor, "Activate semester", semester.code + " is now active.");
    return "Active semester set to " + code + ".";
  }

  private String createCourse(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.ADMIN);
    String code = requiredText(payload, "code").toUpperCase(Locale.ROOT);
    if (coursesByCode.containsKey(code)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course already exists.");
    }

    String semesterCode = requiredText(payload, "semesterCode").toUpperCase(Locale.ROOT);
    if (!semestersByCode.containsKey(semesterCode)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Semester for course is unknown.");
    }

    String lecturerUsername = text(payload, "lecturerUsername");
    String assistantUsername = text(payload, "assistantUsername");

    Course course =
        new Course(
            code,
            requiredText(payload, "name"),
            semesterCode,
            payload.path("ects").asInt(6),
            resolveUserIdByUsername(lecturerUsername, PortalRole.LECTURER),
            resolveUserIdByUsername(assistantUsername, PortalRole.ASSISTANT),
            new ArrayList<>(List.of(new GroupDefinition("G1", "LECTURE", "regular", 150))),
            new LinkedHashMap<>());
    coursesByCode.put(code, course);

    logActivity(actor, "Create course", code + " - " + course.name);
    return "Course " + code + " created.";
  }

  private String assignStaff(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.ADMIN);
    Course course = requireCourse(payload);

    String lecturerUsername = requiredText(payload, "lecturerUsername");
    String assistantUsername = requiredText(payload, "assistantUsername");

    course.lecturerUserId = resolveUserIdByUsername(lecturerUsername, PortalRole.LECTURER);
    course.assistantUserId = resolveUserIdByUsername(assistantUsername, PortalRole.ASSISTANT);

    logActivity(
        actor,
        "Assign course staff",
        course.code + " lecturer=" + lecturerUsername + ", assistant=" + assistantUsername);
    return "Staff assigned for " + course.code + ".";
  }

  private String defineGroup(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.ADMIN, PortalRole.LECTURER);
    Course course = requireCourse(payload);

    if (actor.role() == PortalRole.LECTURER
        && !Objects.equals(course.lecturerUserId, actor.userId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Lecturer can define groups only for owned courses.");
    }

    GroupDefinition group =
        new GroupDefinition(
            requiredText(payload, "groupCode"),
            requiredText(payload, "type").toUpperCase(Locale.ROOT),
            requiredText(payload, "category"),
            payload.path("capacity").asInt(30));

    boolean exists =
        course.groups.stream()
            .anyMatch(candidate -> candidate.groupCode.equalsIgnoreCase(group.groupCode));
    if (exists) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group already exists on course.");
    }

    course.groups.add(group);
    logActivity(
        actor,
        "Define group",
        course.code + " -> " + group.groupCode + " (" + group.category + ")");
    return "Group " + group.groupCode + " added to " + course.code + ".";
  }

  private String importStudents(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.ADMIN, PortalRole.STUSLU);
    JsonNode studentsNode = payload.path("students");
    if (!studentsNode.isArray() || studentsNode.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "students array is required.");
    }

    int imported = 0;
    for (JsonNode studentNode : studentsNode) {
      String jmbag = requiredText(studentNode, "jmbag");
      String fullName = requiredText(studentNode, "fullName");
      int year = studentNode.path("yearOfStudy").asInt(1);

      boolean exists =
          studentsById.values().stream().anyMatch(student -> student.jmbag.equalsIgnoreCase(jmbag));
      if (exists) {
        continue;
      }

      long userId = userIdSequence.incrementAndGet();
      String generatedUsername = "student." + jmbag.substring(Math.max(0, jmbag.length() - 4));
      String normalizedUsername = uniqueUsername(generatedUsername);

      PortalUser user =
          new PortalUser(
              userId,
              normalizedUsername,
              passwordEncoder.encode(DEFAULT_PASSWORD),
              fullName,
              PortalRole.STUDENT,
              jmbag,
              new LinkedHashSet<>());
      usersByUsername.put(normalizedUsername, user);

      StudentProfile profile =
          new StudentProfile(
              userId, jmbag, fullName, year, "Computer Science", new LinkedHashMap<>());
      studentsById.put(userId, profile);
      imported++;
    }

    logActivity(actor, "Import students", imported + " new student(s) imported.");
    return imported + " student(s) imported.";
  }

  private String syncOperation(PortalSessionUser actor, String operationName) {
    requireRole(actor, PortalRole.ADMIN);
    SyncOperation operation = syncOperations.get(operationName);
    if (operation == null) {
      operation = new SyncOperation(operationName, "never", "SUCCESS");
      syncOperations.put(operationName, operation);
    }
    operation.lastRunAt = now();
    operation.lastStatus = "SUCCESS";

    logActivity(actor, operationName, "Synchronization finished successfully.");
    return operationName + " executed.";
  }

  private String enterPoints(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.LECTURER, PortalRole.ASSISTANT);
    String courseCode = requiredText(payload, "courseCode").toUpperCase(Locale.ROOT);
    Course course =
        Optional.ofNullable(coursesByCode.get(courseCode))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

    if (actor.role() == PortalRole.LECTURER
        && !Objects.equals(course.lecturerUserId, actor.userId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lecturer does not own this course.");
    }
    if (actor.role() == PortalRole.ASSISTANT
        && !Objects.equals(course.assistantUserId, actor.userId())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Assistant is not assigned to this course.");
    }

    StudentProfile student = requireStudentByJmbag(requiredText(payload, "studentJmbag"));
    double points = payload.path("points").asDouble(-1);
    double maxPoints = payload.path("maxPoints").asDouble(-1);
    if (points < 0 || maxPoints <= 0 || points > maxPoints) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid points or maxPoints.");
    }

    PointEntry entry =
        new PointEntry(
            pointIdSequence.incrementAndGet(),
            courseCode,
            student.userId,
            requiredText(payload, "component"),
            points,
            maxPoints,
            false,
            actor.fullName());
    pointsById.put(entry.id, entry);

    logActivity(
        actor,
        "Enter points",
        courseCode
            + " / "
            + student.jmbag
            + " / "
            + entry.component
            + " = "
            + points
            + "/"
            + maxPoints);
    return "Points entered for " + student.fullName + " on " + courseCode + ".";
  }

  private String publishPoints(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.LECTURER, PortalRole.ADMIN);
    String courseCode = requiredText(payload, "courseCode").toUpperCase(Locale.ROOT);

    if (actor.role() == PortalRole.LECTURER) {
      Course course =
          Optional.ofNullable(coursesByCode.get(courseCode))
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
      if (!Objects.equals(course.lecturerUserId, actor.userId())) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Lecturer does not own this course.");
      }
    }

    long affected =
        pointsById.values().stream()
            .filter(point -> courseCode.equalsIgnoreCase(point.courseCode))
            .peek(point -> point.published = true)
            .count();

    logActivity(actor, "Publish points", courseCode + " -> " + affected + " entry(ies) published.");
    return "Published points for " + courseCode + ".";
  }

  private String createExam(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.LECTURER, PortalRole.ADMIN);
    String courseCode = requiredText(payload, "courseCode").toUpperCase(Locale.ROOT);
    Course course =
        Optional.ofNullable(coursesByCode.get(courseCode))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

    if (actor.role() == PortalRole.LECTURER
        && !Objects.equals(course.lecturerUserId, actor.userId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Lecturer does not own this course.");
    }

    ExamEvent exam =
        new ExamEvent(
            examIdSequence.incrementAndGet(),
            courseCode,
            requiredText(payload, "title"),
            requiredDateTime(payload, "dateTime"),
            requiredText(payload, "room"),
            payload.path("capacity").asInt(80),
            false,
            countEnrolledStudents(courseCode));
    examsById.put(exam.id, exam);

    logActivity(actor, "Create exam", exam.courseCode + " / " + exam.title + " / " + exam.room);
    return "Exam created for " + exam.courseCode + ".";
  }

  private String publishExam(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.LECTURER, PortalRole.ADMIN);
    long examId = requiredLong(payload, "examId");
    ExamEvent exam =
        Optional.ofNullable(examsById.get(examId))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found."));

    if (actor.role() == PortalRole.LECTURER) {
      Course course =
          Optional.ofNullable(coursesByCode.get(exam.courseCode))
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
      if (!Objects.equals(course.lecturerUserId, actor.userId())) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Lecturer does not own this course.");
      }
    }

    exam.published = true;
    logActivity(actor, "Publish exam results", exam.courseCode + " / " + exam.title);
    return "Exam results published.";
  }

  private String requestGroupExchange(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.STUDENT);
    StudentProfile student =
        Optional.ofNullable(studentsById.get(actor.userId()))
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Student profile missing."));

    String courseCode = requiredText(payload, "courseCode").toUpperCase(Locale.ROOT);
    String fromGroup = requiredText(payload, "fromGroup");
    String toGroup = requiredText(payload, "toGroup");
    if (!courseCodeExists(courseCode)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found.");
    }

    GroupExchangeRequest exchange =
        new GroupExchangeRequest(
            exchangeIdSequence.incrementAndGet(),
            courseCode,
            student.userId,
            fromGroup,
            toGroup,
            "PENDING",
            text(payload, "reason"),
            "");
    groupExchangesById.put(exchange.id, exchange);

    logActivity(actor, "Request group exchange", courseCode + " " + fromGroup + " -> " + toGroup);
    return "Group exchange request submitted.";
  }

  private String decideGroupExchange(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.ADMIN, PortalRole.STUSLU);
    long exchangeId = requiredLong(payload, "exchangeId");
    String decision = requiredText(payload, "decision").toUpperCase(Locale.ROOT);

    GroupExchangeRequest exchange =
        Optional.ofNullable(groupExchangesById.get(exchangeId))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exchange not found."));

    if (!Set.of("APPROVED", "REJECTED").contains(decision)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Decision must be APPROVED or REJECTED.");
    }

    exchange.status = decision;
    exchange.decidedBy = actor.fullName();
    if ("APPROVED".equals(decision)) {
      StudentProfile student = studentsById.get(exchange.studentId);
      if (student != null) {
        student.groupsByCourse.put(exchange.courseCode, exchange.toGroup);
      }
    }

    logActivity(actor, "Decide exchange", "#" + exchange.id + " -> " + decision);
    return "Exchange decision saved.";
  }

  private String enrollStudent(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.ADMIN, PortalRole.STUSLU);
    String jmbag = requiredText(payload, "studentJmbag");
    String courseCode = requiredText(payload, "courseCode").toUpperCase(Locale.ROOT);

    StudentProfile student = requireStudentByJmbag(jmbag);
    Course course =
        Optional.ofNullable(coursesByCode.get(courseCode))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

    student.enrolledCourseCodes.add(courseCode);
    PortalUser user = userById(student.userId);
    user.enrolledCourseCodes.add(courseCode);

    course.enrolledStudentIds.put(student.userId, defaultGroupForCourse(course));

    logActivity(actor, "Enroll student", jmbag + " -> " + courseCode);
    return "Student enrolled to " + courseCode + ".";
  }

  private String assignStudentGroup(PortalSessionUser actor, JsonNode payload) {
    requireRole(actor, PortalRole.ADMIN, PortalRole.STUSLU);
    String jmbag = requiredText(payload, "studentJmbag");
    String courseCode = requiredText(payload, "courseCode").toUpperCase(Locale.ROOT);
    String groupCode = requiredText(payload, "groupCode");

    StudentProfile student = requireStudentByJmbag(jmbag);
    Course course =
        Optional.ofNullable(coursesByCode.get(courseCode))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));

    boolean groupExists =
        course.groups.stream().anyMatch(group -> group.groupCode.equalsIgnoreCase(groupCode));
    if (!groupExists) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Group does not exist for course.");
    }

    student.groupsByCourse.put(courseCode, groupCode);
    course.enrolledStudentIds.put(student.userId, groupCode);

    logActivity(actor, "Assign student group", jmbag + " -> " + courseCode + " / " + groupCode);
    return "Student group assignment updated.";
  }

  private List<CourseView> resolveMyCourses(PortalSessionUser sessionUser) {
    PortalRole role = sessionUser.role();
    return switch (role) {
      case STUDENT -> {
        StudentProfile student = studentsById.get(sessionUser.userId());
        if (student == null) {
          yield List.of();
        }
        yield student.enrolledCourseCodes.stream()
            .map(coursesByCode::get)
            .filter(Objects::nonNull)
            .map(this::toCourseView)
            .toList();
      }
      case LECTURER ->
          coursesByCode.values().stream()
              .filter(course -> Objects.equals(course.lecturerUserId, sessionUser.userId()))
              .map(this::toCourseView)
              .toList();
      case ASSISTANT ->
          coursesByCode.values().stream()
              .filter(course -> Objects.equals(course.assistantUserId, sessionUser.userId()))
              .map(this::toCourseView)
              .toList();
      case ADMIN, STUSLU -> coursesByCode.values().stream().map(this::toCourseView).toList();
    };
  }

  private List<StudentView> resolveVisibleStudents(PortalSessionUser sessionUser) {
    return switch (sessionUser.role()) {
      case STUDENT -> {
        StudentProfile student = studentsById.get(sessionUser.userId());
        yield student == null ? List.of() : List.of(toStudentView(student));
      }
      case LECTURER ->
          studentsById.values().stream()
              .filter(
                  student -> teachesAnyCourse(sessionUser.userId(), student.enrolledCourseCodes))
              .map(this::toStudentView)
              .toList();
      case ASSISTANT ->
          studentsById.values().stream()
              .filter(
                  student -> assistsAnyCourse(sessionUser.userId(), student.enrolledCourseCodes))
              .map(this::toStudentView)
              .toList();
      case ADMIN, STUSLU -> studentsById.values().stream().map(this::toStudentView).toList();
    };
  }

  private List<UserView> resolveVisibleUsers(PortalSessionUser sessionUser) {
    return switch (sessionUser.role()) {
      case ADMIN, STUSLU ->
          usersByUsername.values().stream()
              .map(this::toUserView)
              .sorted(Comparator.comparing(UserView::role).thenComparing(UserView::fullName))
              .toList();
      case STUDENT, LECTURER, ASSISTANT ->
          usersByUsername.values().stream()
              .filter(user -> user.userId == sessionUser.userId())
              .map(this::toUserView)
              .toList();
    };
  }

  private List<ScheduleView> resolveVisibleSchedule(PortalSessionUser sessionUser) {
    return switch (sessionUser.role()) {
      case STUDENT -> {
        StudentProfile student = studentsById.get(sessionUser.userId());
        if (student == null) {
          yield List.of();
        }
        yield scheduleById.values().stream()
            .filter(event -> student.enrolledCourseCodes.contains(event.courseCode))
            .filter(
                event -> {
                  if (!"LAB".equals(event.type)) {
                    return true;
                  }
                  String assignedGroup = student.groupsByCourse.get(event.courseCode);
                  return assignedGroup == null || assignedGroup.equalsIgnoreCase(event.groupCode);
                })
            .sorted(
                Comparator.comparing((ScheduleEvent event) -> event.weekday)
                    .thenComparing(event -> event.startsAt))
            .map(this::toScheduleView)
            .toList();
      }
      case LECTURER ->
          scheduleById.values().stream()
              .filter(event -> lecturerOwnsCourse(sessionUser.userId(), event.courseCode))
              .sorted(
                  Comparator.comparing((ScheduleEvent event) -> event.weekday)
                      .thenComparing(event -> event.startsAt))
              .map(this::toScheduleView)
              .toList();
      case ASSISTANT ->
          scheduleById.values().stream()
              .filter(event -> assistantAssignedToCourse(sessionUser.userId(), event.courseCode))
              .sorted(
                  Comparator.comparing((ScheduleEvent event) -> event.weekday)
                      .thenComparing(event -> event.startsAt))
              .map(this::toScheduleView)
              .toList();
      case ADMIN, STUSLU ->
          scheduleById.values().stream()
              .sorted(
                  Comparator.comparing((ScheduleEvent event) -> event.weekday)
                      .thenComparing(event -> event.startsAt))
              .map(this::toScheduleView)
              .toList();
    };
  }

  private List<PointView> resolveVisiblePoints(PortalSessionUser sessionUser) {
    return switch (sessionUser.role()) {
      case STUDENT ->
          pointsById.values().stream()
              .filter(point -> point.studentId == sessionUser.userId())
              .map(this::toPointView)
              .toList();
      case LECTURER ->
          pointsById.values().stream()
              .filter(point -> lecturerOwnsCourse(sessionUser.userId(), point.courseCode))
              .map(this::toPointView)
              .toList();
      case ASSISTANT ->
          pointsById.values().stream()
              .filter(point -> assistantAssignedToCourse(sessionUser.userId(), point.courseCode))
              .map(this::toPointView)
              .toList();
      case ADMIN, STUSLU -> pointsById.values().stream().map(this::toPointView).toList();
    };
  }

  private List<GradeOverviewView> buildGradeOverview(List<PointView> visiblePoints) {
    Map<String, GradeAccumulator> gradesByStudentAndCourse = new LinkedHashMap<>();

    for (PointView point : visiblePoints) {
      String key = point.studentJmbag() + "::" + point.courseCode();
      GradeAccumulator accumulator =
          gradesByStudentAndCourse.computeIfAbsent(key, ignored -> new GradeAccumulator());
      accumulator.studentJmbag = point.studentJmbag();
      accumulator.studentName = point.studentName();
      accumulator.courseCode = point.courseCode();
      accumulator.collected += point.points();
      accumulator.maximum += point.maxPoints();
      accumulator.published &= point.published();
    }

    return gradesByStudentAndCourse.values().stream()
        .map(GradeAccumulator::toView)
        .sorted(
            Comparator.comparing(GradeOverviewView::courseCode)
                .thenComparing(GradeOverviewView::studentName))
        .toList();
  }

  private List<ExamView> resolveVisibleExams(PortalSessionUser sessionUser) {
    return switch (sessionUser.role()) {
      case STUDENT ->
          examsById.values().stream()
              .filter(exam -> studentEnrolledToCourse(sessionUser.userId(), exam.courseCode))
              .map(this::toExamView)
              .toList();
      case LECTURER ->
          examsById.values().stream()
              .filter(exam -> lecturerOwnsCourse(sessionUser.userId(), exam.courseCode))
              .map(this::toExamView)
              .toList();
      case ASSISTANT ->
          examsById.values().stream()
              .filter(exam -> assistantAssignedToCourse(sessionUser.userId(), exam.courseCode))
              .map(this::toExamView)
              .toList();
      case ADMIN, STUSLU -> examsById.values().stream().map(this::toExamView).toList();
    };
  }

  private List<GroupExchangeView> resolveVisibleGroupExchanges(PortalSessionUser sessionUser) {
    return switch (sessionUser.role()) {
      case STUDENT ->
          groupExchangesById.values().stream()
              .filter(exchange -> exchange.studentId == sessionUser.userId())
              .map(this::toGroupExchangeView)
              .toList();
      case LECTURER ->
          groupExchangesById.values().stream()
              .filter(exchange -> lecturerOwnsCourse(sessionUser.userId(), exchange.courseCode))
              .map(this::toGroupExchangeView)
              .toList();
      case ASSISTANT ->
          groupExchangesById.values().stream()
              .filter(
                  exchange -> assistantAssignedToCourse(sessionUser.userId(), exchange.courseCode))
              .map(this::toGroupExchangeView)
              .toList();
      case ADMIN, STUSLU ->
          groupExchangesById.values().stream().map(this::toGroupExchangeView).toList();
    };
  }

  private List<PermissionView> buildPermissionsView() {
    return coursesByCode.values().stream()
        .map(
            course ->
                new PermissionView(
                    course.code,
                    nameOfUser(course.lecturerUserId),
                    nameOfUser(course.assistantUserId),
                    List.of("ENTER_POINTS", "PUBLISH_POINTS", "MANAGE_EXAMS", "VIEW_ENROLLMENTS")))
        .toList();
  }

  private List<StatCardView> buildCards(
      PortalSessionUser sessionUser,
      List<CourseView> myCourses,
      List<StudentView> visibleStudents,
      List<ExamView> visibleExams) {
    return switch (sessionUser.role()) {
      case STUDENT ->
          List.of(
              new StatCardView(
                  "Enrolled courses",
                  String.valueOf(myCourses.size()),
                  "Current semester workload"),
              new StatCardView(
                  "Upcoming events",
                  String.valueOf(resolveVisibleSchedule(sessionUser).size()),
                  "Lectures, labs, and exams"),
              new StatCardView(
                  "Exam windows",
                  String.valueOf(visibleExams.size()),
                  "Scheduled and announced exams"));
      case LECTURER ->
          List.of(
              new StatCardView(
                  "Teaching courses",
                  String.valueOf(myCourses.size()),
                  "Courses with grading ownership"),
              new StatCardView(
                  "Students in scope",
                  String.valueOf(visibleStudents.size()),
                  "Students enrolled to owned courses"),
              new StatCardView(
                  "Exam slots",
                  String.valueOf(visibleExams.size()),
                  "Exam planning and publishing"));
      case ASSISTANT ->
          List.of(
              new StatCardView(
                  "Lab-supported courses",
                  String.valueOf(myCourses.size()),
                  "Courses with active labs"),
              new StatCardView(
                  "Lab participants",
                  String.valueOf(visibleStudents.size()),
                  "Students in assigned groups"),
              new StatCardView(
                  "Lab sessions",
                  String.valueOf(resolveVisibleSchedule(sessionUser).size()),
                  "Weekly workload"));
      case ADMIN ->
          List.of(
              new StatCardView("Active semester", activeSemesterCode, "Semester lifecycle control"),
              new StatCardView(
                  "Total courses", String.valueOf(coursesByCode.size()), "Cross-course governance"),
              new StatCardView(
                  "Synchronization jobs",
                  String.valueOf(syncOperations.size()),
                  "Students, rooms, lectures, laboratories"));
      case STUSLU ->
          List.of(
              new StatCardView(
                  "Students", String.valueOf(studentsById.size()), "Enrollment and records"),
              new StatCardView(
                  "Course enrollments",
                  String.valueOf(totalEnrollments()),
                  "Active enrollment entries"),
              new StatCardView(
                  "Pending exchanges",
                  String.valueOf(pendingExchanges()),
                  "Administrative decisions"));
    };
  }

  private int totalEnrollments() {
    return studentsById.values().stream()
        .mapToInt(student -> student.enrolledCourseCodes.size())
        .sum();
  }

  private int pendingExchanges() {
    return (int)
        groupExchangesById.values().stream()
            .filter(exchange -> "PENDING".equals(exchange.status))
            .count();
  }

  private List<NavigationItemView> navigationForRole(PortalRole role) {
    List<NavigationItemView> navigation = new ArrayList<>();
    navigation.add(new NavigationItemView("dashboard", "Dashboard", true));
    navigation.add(new NavigationItemView("courses", "Courses", true));
    navigation.add(new NavigationItemView("calendar", "Calendar", true));
    navigation.add(new NavigationItemView("points", "Points", role != PortalRole.STUSLU));
    navigation.add(new NavigationItemView("exams", "Exams", role != PortalRole.STUSLU));
    navigation.add(new NavigationItemView("exchange", "Group Exchange", true));
    navigation.add(
        new NavigationItemView(
            "administration",
            "Administration",
            EnumSet.of(PortalRole.ADMIN, PortalRole.STUSLU).contains(role)));
    return navigation;
  }

  private List<String> enabledActionsForRole(PortalRole role) {
    return switch (role) {
      case STUDENT -> List.of("REQUEST_GROUP_EXCHANGE");
      case LECTURER ->
          List.of(
              "DEFINE_GROUP",
              "ENTER_POINTS",
              "PUBLISH_POINTS",
              "CREATE_EXAM",
              "PUBLISH_EXAM_RESULTS");
      case ASSISTANT -> List.of("ENTER_POINTS", "DEFINE_GROUP");
      case ADMIN ->
          List.of(
              "CREATE_SEMESTER",
              "ACTIVATE_SEMESTER",
              "CREATE_COURSE",
              "ASSIGN_STAFF",
              "DEFINE_GROUP",
              "IMPORT_STUDENTS",
              "SYNC_STUDENTS",
              "SYNC_ROOMS",
              "SYNC_LECTURES",
              "SYNC_LABS",
              "ENTER_POINTS",
              "PUBLISH_POINTS",
              "CREATE_EXAM",
              "PUBLISH_EXAM_RESULTS",
              "DECIDE_GROUP_EXCHANGE",
              "ENROLL_STUDENT",
              "ASSIGN_STUDENT_GROUP");
      case STUSLU ->
          List.of(
              "IMPORT_STUDENTS", "DECIDE_GROUP_EXCHANGE", "ENROLL_STUDENT", "ASSIGN_STUDENT_GROUP");
    };
  }

  private List<DemoAccountView> demoAccounts() {
    return List.of(
        new DemoAccountView("student.ana", DEFAULT_PASSWORD, "STUDENT", "Ana Horvat"),
        new DemoAccountView("lecturer.marko", DEFAULT_PASSWORD, "LECTURER", "Marko Cupic"),
        new DemoAccountView("assistant.iva", DEFAULT_PASSWORD, "ASSISTANT", "Iva Kovacevic"),
        new DemoAccountView("stuslu.sara", DEFAULT_PASSWORD, "STUSLU", "Sara Peric"),
        new DemoAccountView("admin.ferko", DEFAULT_PASSWORD, "ADMIN", "Ferko Admin"));
  }

  private CurrentSemesterView currentSemesterView() {
    Semester active =
        Optional.ofNullable(semestersByCode.get(activeSemesterCode))
            .orElseThrow(() -> new IllegalStateException("Active semester is not initialized."));
    return new CurrentSemesterView(active.code, active.academicYear, active.term);
  }

  private AuthView toAuthView(PortalUser user) {
    return new AuthView(
        user.userId,
        user.username,
        user.fullName,
        user.role.name(),
        redirectSectionForRole(user.role),
        enabledActionsForRole(user.role));
  }

  private String redirectSectionForRole(PortalRole role) {
    return switch (role) {
      case STUDENT -> "dashboard";
      case LECTURER, ASSISTANT -> "courses";
      case ADMIN, STUSLU -> "administration";
    };
  }

  private CourseView toCourseView(Course course) {
    return new CourseView(
        course.code,
        course.name,
        course.ects,
        course.semesterCode,
        nameOfUser(course.lecturerUserId),
        nameOfUser(course.assistantUserId),
        course.groups.stream().map(GroupDefinition::toView).toList());
  }

  private StudentView toStudentView(StudentProfile student) {
    return new StudentView(
        student.userId,
        student.jmbag,
        student.fullName,
        student.yearOfStudy,
        student.studyProgram,
        List.copyOf(student.enrolledCourseCodes),
        Map.copyOf(student.groupsByCourse));
  }

  private UserView toUserView(PortalUser user) {
    return new UserView(user.userId, user.username, user.fullName, user.role.name(), user.jmbag);
  }

  private ScheduleView toScheduleView(ScheduleEvent event) {
    return new ScheduleView(
        event.id,
        event.courseCode,
        event.type,
        event.groupCode,
        event.category,
        event.room,
        event.weekday,
        event.startsAt.toString(),
        event.endsAt.toString(),
        event.instructor);
  }

  private PointView toPointView(PointEntry point) {
    StudentProfile student = studentsById.get(point.studentId);
    double percentage = point.maxPoints == 0 ? 0 : (point.points / point.maxPoints) * 100;
    return new PointView(
        point.id,
        point.courseCode,
        point.component,
        student == null ? "-" : student.jmbag,
        student == null ? "-" : student.fullName,
        point.points,
        point.maxPoints,
        Math.round(percentage * 100.0) / 100.0,
        point.published,
        point.enteredBy);
  }

  private ExamView toExamView(ExamEvent exam) {
    return new ExamView(
        exam.id,
        exam.courseCode,
        exam.title,
        DATE_TIME_FORMAT.format(exam.dateTime),
        exam.room,
        exam.capacity,
        exam.allocatedStudents,
        exam.published);
  }

  private GroupExchangeView toGroupExchangeView(GroupExchangeRequest exchange) {
    StudentProfile student = studentsById.get(exchange.studentId);
    return new GroupExchangeView(
        exchange.id,
        exchange.courseCode,
        student == null ? "-" : student.fullName,
        student == null ? "-" : student.jmbag,
        exchange.fromGroup,
        exchange.toGroup,
        exchange.status,
        exchange.reason,
        exchange.decidedBy);
  }

  private void requireRole(PortalSessionUser actor, PortalRole... allowedRoles) {
    Set<PortalRole> allowed = EnumSet.noneOf(PortalRole.class);
    allowed.addAll(List.of(allowedRoles));
    if (!allowed.contains(actor.role())) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Action is not allowed for role " + actor.role());
    }
  }

  private Course requireCourse(JsonNode payload) {
    String courseCode = requiredText(payload, "courseCode").toUpperCase(Locale.ROOT);
    return Optional.ofNullable(coursesByCode.get(courseCode))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found."));
  }

  private StudentProfile requireStudentByJmbag(String jmbag) {
    return studentsById.values().stream()
        .filter(student -> student.jmbag.equalsIgnoreCase(jmbag))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found."));
  }

  private boolean courseCodeExists(String courseCode) {
    return coursesByCode.containsKey(courseCode.toUpperCase(Locale.ROOT));
  }

  private boolean lecturerOwnsCourse(long lecturerId, String courseCode) {
    Course course = coursesByCode.get(courseCode);
    return course != null && Objects.equals(course.lecturerUserId, lecturerId);
  }

  private boolean assistantAssignedToCourse(long assistantId, String courseCode) {
    Course course = coursesByCode.get(courseCode);
    return course != null && Objects.equals(course.assistantUserId, assistantId);
  }

  private boolean studentEnrolledToCourse(long studentId, String courseCode) {
    StudentProfile student = studentsById.get(studentId);
    return student != null && student.enrolledCourseCodes.contains(courseCode);
  }

  private boolean teachesAnyCourse(long lecturerId, Set<String> courses) {
    return courses.stream().anyMatch(courseCode -> lecturerOwnsCourse(lecturerId, courseCode));
  }

  private boolean assistsAnyCourse(long assistantId, Set<String> courses) {
    return courses.stream()
        .anyMatch(courseCode -> assistantAssignedToCourse(assistantId, courseCode));
  }

  private long countEnrolledStudents(String courseCode) {
    return studentsById.values().stream()
        .filter(student -> student.enrolledCourseCodes.contains(courseCode))
        .count();
  }

  private String defaultGroupForCourse(Course course) {
    return course.groups.isEmpty() ? "G1" : course.groups.get(0).groupCode;
  }

  private String nameOfUser(Long userId) {
    if (userId == null) {
      return "-";
    }
    return usersByUsername.values().stream()
        .filter(user -> user.userId == userId)
        .map(user -> user.fullName)
        .findFirst()
        .orElse("-");
  }

  private PortalUser userById(long userId) {
    return usersByUsername.values().stream()
        .filter(user -> user.userId == userId)
        .findFirst()
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Session user no longer exists."));
  }

  private long resolveUserIdByUsername(String username, PortalRole requiredRole) {
    if (isBlank(username)) {
      return requiredRole == PortalRole.ASSISTANT ? 3001L : 2001L;
    }
    PortalUser user = usersByUsername.get(username.trim().toLowerCase(Locale.ROOT));
    if (user == null || user.role != requiredRole) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "User " + username + " must exist with role " + requiredRole.name());
    }
    return user.userId;
  }

  private String uniqueUsername(String baseUsername) {
    String normalized = baseUsername.toLowerCase(Locale.ROOT);
    if (!usersByUsername.containsKey(normalized)) {
      return normalized;
    }
    long suffix = 2;
    while (usersByUsername.containsKey(normalized + suffix)) {
      suffix++;
    }
    return normalized + suffix;
  }

  private void logActivity(PortalSessionUser actor, String action, String details) {
    addActivity(new ActivityLogEntry(now(), actor.fullName(), action, details));
  }

  private void addActivity(ActivityLogEntry entry) {
    activityLog.addFirst(entry);
    while (activityLog.size() > 100) {
      activityLog.removeLast();
    }
  }

  private String now() {
    return DATE_TIME_FORMAT.format(LocalDateTime.now());
  }

  private String requiredText(JsonNode payload, String field) {
    String value = text(payload, field);
    if (isBlank(value)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required.");
    }
    return value.trim();
  }

  private String text(JsonNode payload, String field) {
    JsonNode node = payload == null ? null : payload.path(field);
    if (node == null || node.isMissingNode() || node.isNull()) {
      return "";
    }
    String value = node.asText("");
    return value == null ? "" : value.trim();
  }

  private long requiredLong(JsonNode payload, String field) {
    JsonNode node = payload == null ? null : payload.path(field);
    if (node == null || node.isMissingNode() || node.isNull()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required.");
    }
    long value = node.asLong(Long.MIN_VALUE);
    if (value == Long.MIN_VALUE) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " must be a number.");
    }
    return value;
  }

  private LocalDate requiredDate(JsonNode payload, String field) {
    try {
      return LocalDate.parse(requiredText(payload, field), DATE_FORMAT);
    } catch (RuntimeException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, field + " must be ISO date (yyyy-MM-dd).", ex);
    }
  }

  private LocalDateTime requiredDateTime(JsonNode payload, String field) {
    try {
      return LocalDateTime.parse(requiredText(payload, field), DATE_TIME_FORMAT);
    } catch (RuntimeException ex) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, field + " must be ISO datetime (yyyy-MM-ddTHH:mm:ss).", ex);
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private void seedUsersAndStudents() {
    createUser(1001L, "student.ana", "Ana Horvat", PortalRole.STUDENT, "0036501001");
    createUser(1002L, "student.luka", "Luka Marinic", PortalRole.STUDENT, "0036501002");
    createUser(1003L, "student.ivan", "Ivan Babic", PortalRole.STUDENT, "0036501003");

    createUser(2001L, "lecturer.marko", "Marko Cupic", PortalRole.LECTURER, "");
    createUser(3001L, "assistant.iva", "Iva Kovacevic", PortalRole.ASSISTANT, "");
    createUser(9101L, "stuslu.sara", "Sara Peric", PortalRole.STUSLU, "");
    createUser(9001L, "admin.ferko", "Ferko Admin", PortalRole.ADMIN, "");

    studentsById.put(
        1001L,
        new StudentProfile(
            1001L, "0036501001", "Ana Horvat", 2, "Computer Science", new LinkedHashMap<>()));
    studentsById.put(
        1002L,
        new StudentProfile(
            1002L, "0036501002", "Luka Marinic", 2, "Computer Science", new LinkedHashMap<>()));
    studentsById.put(
        1003L,
        new StudentProfile(
            1003L, "0036501003", "Ivan Babic", 1, "Computing", new LinkedHashMap<>()));
  }

  private void seedAcademicStructure() {
    semestersByCode.put(
        "2025Z",
        new Semester(
            "2025Z",
            "2025/2026",
            "winter",
            LocalDate.of(2025, 10, 6),
            LocalDate.of(2026, 2, 13),
            true));
    semestersByCode.put(
        "2025L",
        new Semester(
            "2025L",
            "2025/2026",
            "summer",
            LocalDate.of(2026, 2, 23),
            LocalDate.of(2026, 6, 26),
            false));
    activeSemesterCode = "2025Z";

    Course utr =
        new Course(
            "UTR",
            "Uvod u teoriju racunarstva",
            "2025Z",
            6,
            2001L,
            3001L,
            new ArrayList<>(
                List.of(
                    new GroupDefinition("P1", "LECTURE", "regular", 180),
                    new GroupDefinition("L1", "LAB", "klasicno", 24),
                    new GroupDefinition("L2", "LAB", "klasicno", 24),
                    new GroupDefinition("D1", "LAB", "demosi", 16))),
            new LinkedHashMap<>());

    Course oo =
        new Course(
            "OOUP",
            "Objektno orijentirano programiranje",
            "2025Z",
            7,
            2001L,
            3001L,
            new ArrayList<>(
                List.of(
                    new GroupDefinition("P1", "LECTURE", "regular", 200),
                    new GroupDefinition("LAB-A", "LAB", "klasicno", 30),
                    new GroupDefinition("LAB-B", "LAB", "demosi", 20))),
            new LinkedHashMap<>());

    Course dismat =
        new Course(
            "DISMAT",
            "Diskretna matematika",
            "2025Z",
            5,
            2001L,
            3001L,
            new ArrayList<>(
                List.of(
                    new GroupDefinition("G1", "LECTURE", "regular", 220),
                    new GroupDefinition("VJ1", "LAB", "klasicno", 28),
                    new GroupDefinition("VJ2", "LAB", "klasicno", 28))),
            new LinkedHashMap<>());

    coursesByCode.put(utr.code, utr);
    coursesByCode.put(oo.code, oo);
    coursesByCode.put(dismat.code, dismat);

    enrollSeedStudent(1001L, "UTR", "L1");
    enrollSeedStudent(1001L, "OOUP", "LAB-A");
    enrollSeedStudent(1002L, "UTR", "L2");
    enrollSeedStudent(1002L, "OOUP", "LAB-B");
    enrollSeedStudent(1003L, "DISMAT", "VJ1");
  }

  private void seedSchedule() {
    addSchedule(
        "UTR", "LECTURE", "P1", "regular", "A-111", "MONDAY", "08:15", "10:00", "Marko Cupic");
    addSchedule(
        "UTR", "LAB", "L1", "klasicno", "C-03", "WEDNESDAY", "12:15", "14:00", "Iva Kovacevic");
    addSchedule(
        "UTR", "LAB", "L2", "klasicno", "C-04", "WEDNESDAY", "12:15", "14:00", "Iva Kovacevic");
    addSchedule(
        "UTR", "LAB", "D1", "demosi", "C-05", "WEDNESDAY", "14:15", "16:00", "Iva Kovacevic");

    addSchedule(
        "OOUP", "LECTURE", "P1", "regular", "B-201", "TUESDAY", "10:15", "12:00", "Marko Cupic");
    addSchedule(
        "OOUP", "LAB", "LAB-A", "klasicno", "C-07", "THURSDAY", "08:15", "10:00", "Iva Kovacevic");
    addSchedule(
        "OOUP", "LAB", "LAB-B", "demosi", "C-08", "THURSDAY", "08:15", "10:00", "Iva Kovacevic");

    addSchedule(
        "DISMAT", "LECTURE", "G1", "regular", "D-150", "FRIDAY", "12:15", "14:00", "Marko Cupic");
    addSchedule(
        "DISMAT", "LAB", "VJ1", "klasicno", "C-12", "FRIDAY", "14:15", "16:00", "Iva Kovacevic");
    addSchedule(
        "DISMAT", "LAB", "VJ2", "klasicno", "C-13", "FRIDAY", "14:15", "16:00", "Iva Kovacevic");
  }

  private void seedPoints() {
    addPoint("UTR", 1001L, "Midterm", 27, 40, true, "Marko Cupic");
    addPoint("UTR", 1001L, "Lab", 18, 20, true, "Iva Kovacevic");
    addPoint("UTR", 1001L, "Project", 32, 40, false, "Marko Cupic");

    addPoint("OOUP", 1001L, "Midterm", 35, 50, true, "Marko Cupic");
    addPoint("OOUP", 1001L, "Lab", 42, 50, false, "Iva Kovacevic");

    addPoint("UTR", 1002L, "Midterm", 31, 40, true, "Marko Cupic");
    addPoint("UTR", 1002L, "Lab", 16, 20, true, "Iva Kovacevic");
  }

  private void seedExams() {
    addExam("UTR", "Regular exam term", LocalDateTime.of(2026, 2, 2, 9, 0), "A-111", 120, true);
    addExam("OOUP", "Written exam", LocalDateTime.of(2026, 2, 5, 10, 0), "B-201", 150, false);
    addExam(
        "DISMAT", "First exam period", LocalDateTime.of(2026, 2, 7, 11, 0), "D-150", 180, false);
  }

  private void seedGroupExchanges() {
    addGroupExchange("UTR", 1001L, "L1", "L2", "Need schedule overlap fix.", "PENDING", "");
    addGroupExchange(
        "OOUP", 1002L, "LAB-B", "LAB-A", "Commute timing issue.", "APPROVED", "Sara Peric");
  }

  private void seedSyncOperations() {
    syncOperations.put(
        "Student synchronization",
        new SyncOperation("Student synchronization", "2026-02-17T08:15:00", "SUCCESS"));
    syncOperations.put(
        "Room synchronization",
        new SyncOperation("Room synchronization", "2026-02-17T08:20:00", "SUCCESS"));
    syncOperations.put(
        "Lecture schedule synchronization",
        new SyncOperation("Lecture schedule synchronization", "2026-02-17T08:30:00", "SUCCESS"));
    syncOperations.put(
        "Laboratory schedule synchronization",
        new SyncOperation("Laboratory schedule synchronization", "2026-02-17T08:32:00", "SUCCESS"));
  }

  private void seedActivity() {
    addActivity(
        new ActivityLogEntry(
            "2026-02-18T09:10:00",
            "Ferko Admin",
            "Semester governance",
            "Active semester confirmed: 2025Z"));
    addActivity(
        new ActivityLogEntry(
            "2026-02-18T09:20:00",
            "Sara Peric",
            "Enrollment update",
            "0036501003 enrolled to DISMAT"));
    addActivity(
        new ActivityLogEntry(
            "2026-02-18T10:05:00", "Marko Cupic", "Points entry", "UTR Midterm points entered"));
    addActivity(
        new ActivityLogEntry(
            "2026-02-18T10:22:00",
            "Iva Kovacevic",
            "Lab synchronization",
            "Parallel lab groups verified (klasicno/demosi)"));
  }

  public synchronized void mergeLegacyBootstrapData(
      LegacyDataset dataset,
      int maxCourses,
      int maxStudents,
      int maxScheduleEntries,
      int maxExamEntries) {
    if (dataset == null || dataset.enrollments().isEmpty()) {
      return;
    }

    int courseLimit = Math.max(6, maxCourses);
    int studentLimit = Math.max(40, maxStudents);
    int scheduleLimit = Math.max(60, maxScheduleEntries);
    int examLimit = Math.max(20, maxExamEntries);

    ensureActiveSemesterExists();

    Map<String, Integer> courseFrequency = new LinkedHashMap<>();
    Map<String, String> preferredCourseNames = new LinkedHashMap<>();
    for (EnrollmentEntry enrollment : dataset.enrollments()) {
      String courseCode = normalizeCode(enrollment.courseCode());
      if (courseCode.isBlank()) {
        continue;
      }
      courseFrequency.merge(courseCode, 1, Integer::sum);
      if (!isBlank(enrollment.courseName()) && !preferredCourseNames.containsKey(courseCode)) {
        preferredCourseNames.put(courseCode, enrollment.courseName().trim());
      }
    }

    Set<String> selectedCourseCodes =
        courseFrequency.entrySet().stream()
            .sorted(
                Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                    .reversed()
                    .thenComparing(Map.Entry::getKey))
            .limit(courseLimit)
            .map(Map.Entry::getKey)
            .collect(LinkedHashSet::new, Set::add, Set::addAll);
    if (selectedCourseCodes.isEmpty()) {
      return;
    }

    Map<String, StudentProfile> studentsByJmbag = indexStudentsByJmbag();
    int importedStudents = 0;
    int importedEnrollments = 0;

    for (EnrollmentEntry enrollment : dataset.enrollments()) {
      String courseCode = normalizeCode(enrollment.courseCode());
      if (!selectedCourseCodes.contains(courseCode)) {
        continue;
      }

      Course course = coursesByCode.get(courseCode);
      if (course == null) {
        course = createCourseFromLegacyDataset(courseCode, preferredCourseNames, dataset);
        coursesByCode.put(courseCode, course);
      }

      if (!isBlank(enrollment.groupCode())) {
        ensureCourseGroupExists(course, enrollment.groupCode());
      }

      String jmbag = enrollment.jmbag().trim();
      if (jmbag.isBlank()) {
        continue;
      }

      StudentProfile student = studentsByJmbag.get(jmbag);
      if (student == null) {
        if (importedStudents >= studentLimit) {
          continue;
        }
        student = createLegacyStudent(enrollment, studentsByJmbag);
        importedStudents++;
      }

      String assignedGroup =
          chooseGroupCodeForEnrollment(
              course, enrollment.groupCode(), defaultGroupForCourse(course));
      if (!student.enrolledCourseCodes.contains(courseCode)) {
        student.enrolledCourseCodes.add(courseCode);
        student.groupsByCourse.put(courseCode, assignedGroup);
        course.enrolledStudentIds.put(student.userId, assignedGroup);
        userById(student.userId).enrolledCourseCodes.add(courseCode);
        importedEnrollments++;
      }
    }

    mergeLegacySchedule(dataset.schedules(), selectedCourseCodes, scheduleLimit);
    mergeLegacyExams(dataset.exams(), selectedCourseCodes, examLimit);
    seedLegacyPoints(selectedCourseCodes, importedStudents);
    seedLegacyGroupExchanges(selectedCourseCodes);
    syncOperations.put(
        "Legacy dataset bootstrap",
        new SyncOperation(
            "Legacy dataset bootstrap",
            now(),
            "SUCCESS ("
                + importedStudents
                + " students / "
                + importedEnrollments
                + " enrollments)"));
    addActivity(
        new ActivityLogEntry(
            now(),
            "Ferko Bootstrap",
            "Legacy dataset import",
            "Courses="
                + selectedCourseCodes.size()
                + ", students="
                + importedStudents
                + ", enrollments="
                + importedEnrollments));
  }

  private void ensureActiveSemesterExists() {
    if (activeSemesterCode != null && semestersByCode.containsKey(activeSemesterCode)) {
      return;
    }
    if (semestersByCode.isEmpty()) {
      Semester fallbackSemester =
          new Semester(
              "2025Z",
              "2025/2026",
              "winter",
              LocalDate.of(2025, 10, 6),
              LocalDate.of(2026, 2, 13),
              true);
      semestersByCode.put(fallbackSemester.code, fallbackSemester);
      activeSemesterCode = fallbackSemester.code;
      return;
    }
    activeSemesterCode = semestersByCode.values().iterator().next().code;
  }

  private Course createCourseFromLegacyDataset(
      String courseCode, Map<String, String> preferredCourseNames, LegacyDataset dataset) {
    CourseCatalogEntry metadata = dataset.courses().get(courseCode);
    String courseName =
        preferredCourseNames.getOrDefault(courseCode, fallbackCourseTitle(metadata, courseCode));
    int ects = deriveEcts(metadata);
    return new Course(
        courseCode,
        courseName,
        activeSemesterCode,
        ects,
        2001L,
        3001L,
        new ArrayList<>(List.of(new GroupDefinition("P1", "LECTURE", "regular", 220))),
        new LinkedHashMap<>());
  }

  private void ensureCourseGroupExists(Course course, String requestedGroupCode) {
    String normalizedGroupCode = chooseGroupCodeForEnrollment(course, requestedGroupCode, "G1");
    boolean exists =
        course.groups.stream()
            .anyMatch(group -> group.groupCode.equalsIgnoreCase(normalizedGroupCode));
    if (exists) {
      return;
    }
    String category =
        normalizedGroupCode.toUpperCase(Locale.ROOT).contains("DEMOSI") ? "demosi" : "klasicno";
    course.groups.add(new GroupDefinition(normalizedGroupCode, "LAB", category, 30));
  }

  private StudentProfile createLegacyStudent(
      EnrollmentEntry enrollment, Map<String, StudentProfile> studentsByJmbag) {
    String jmbag = enrollment.jmbag().trim();
    long userId = userIdSequence.incrementAndGet();
    String fullName =
        isBlank(enrollment.studentName()) ? "Student " + jmbag : enrollment.studentName().trim();
    String username = uniqueUsername("student." + jmbag);

    usersByUsername.put(
        username,
        new PortalUser(
            userId,
            username,
            passwordEncoder.encode(DEFAULT_PASSWORD),
            fullName,
            PortalRole.STUDENT,
            jmbag,
            new LinkedHashSet<>()));

    StudentProfile profile =
        new StudentProfile(
            userId,
            jmbag,
            fullName,
            clampYearOfStudy(enrollment.yearOfStudy()),
            "Computing",
            new LinkedHashMap<>());
    studentsById.put(userId, profile);
    studentsByJmbag.put(jmbag, profile);
    return profile;
  }

  private void mergeLegacySchedule(
      List<ScheduleEntry> scheduleEntries, Set<String> selectedCourses, int maxScheduleEntries) {
    Set<String> existingKeys = new LinkedHashSet<>();
    for (ScheduleEvent event : scheduleById.values()) {
      existingKeys.add(
          scheduleKey(
              event.courseCode, event.weekday, event.startsAt, event.endsAt, event.groupCode));
    }

    int inserted = 0;
    for (ScheduleEntry entry : scheduleEntries) {
      if (inserted >= maxScheduleEntries) {
        break;
      }

      String courseCode = normalizeCode(entry.courseCode());
      if (!selectedCourses.contains(courseCode)) {
        continue;
      }

      DayOfWeek dayOfWeek = entry.date().getDayOfWeek();
      LocalTime startsAt = entry.startsAt();
      LocalTime endsAt = startsAt.plusMinutes(Math.max(30, entry.durationMinutes()));
      String groupCode = firstGroup(entry.groupsText());
      String type = groupCode.toUpperCase(Locale.ROOT).startsWith("P") ? "LECTURE" : "LAB";
      String category = "LECTURE".equals(type) ? "regular" : groupCategory(groupCode);
      String room = (entry.institution() + " " + entry.room()).trim();

      String key = scheduleKey(courseCode, dayOfWeek.name(), startsAt, endsAt, groupCode);
      if (!existingKeys.add(key)) {
        continue;
      }

      addSchedule(
          courseCode,
          type,
          groupCode,
          category,
          room,
          dayOfWeek.name(),
          startsAt.toString(),
          endsAt.toString(),
          "Marko Cupic");
      inserted++;
    }
  }

  private void mergeLegacyExams(
      List<ExamEntry> examEntries, Set<String> selectedCourses, int maxExamEntries) {
    Set<String> existing = new LinkedHashSet<>();
    for (ExamEvent exam : examsById.values()) {
      existing.add(exam.courseCode + "|" + exam.dateTime + "|" + exam.title);
    }

    int inserted = 0;
    for (ExamEntry entry : examEntries) {
      if (inserted >= maxExamEntries) {
        break;
      }
      String courseCode = normalizeCode(entry.courseCode());
      if (!selectedCourses.contains(courseCode)) {
        continue;
      }

      String title = entry.termType() + " exam term";
      LocalDateTime dateTime = LocalDateTime.of(entry.date(), entry.startsAt());
      String key = courseCode + "|" + dateTime + "|" + title;
      if (!existing.add(key)) {
        continue;
      }

      int capacity = (int) Math.max(30, countEnrolledStudents(courseCode) + 20);
      addExam(courseCode, title, dateTime, "A-111", capacity, true);
      inserted++;
    }
  }

  private void seedLegacyPoints(Set<String> selectedCourses, int importedStudents) {
    int maxGeneratedPoints = Math.max(80, importedStudents * 2);
    int generated = 0;

    for (StudentProfile student : studentsById.values()) {
      for (String courseCode : student.enrolledCourseCodes) {
        if (generated >= maxGeneratedPoints || !selectedCourses.contains(courseCode)) {
          continue;
        }
        boolean alreadyHasPoints =
            pointsById.values().stream()
                .anyMatch(
                    point ->
                        Objects.equals(point.courseCode, courseCode)
                            && point.studentId == student.userId);
        if (alreadyHasPoints) {
          continue;
        }

        int hashSeed = Math.abs((student.jmbag + "|" + courseCode).hashCode());
        double midterm = 16 + (hashSeed % 2300) / 100.0;
        double lab = 8 + ((hashSeed / 7) % 1100) / 100.0;
        addPoint(
            courseCode, student.userId, "Midterm", Math.min(midterm, 40), 40, true, "Marko Cupic");
        addPoint(courseCode, student.userId, "Lab", Math.min(lab, 20), 20, true, "Iva Kovacevic");
        generated += 2;
      }
    }
  }

  private void seedLegacyGroupExchanges(Set<String> selectedCourses) {
    int created = 0;
    for (StudentProfile student : studentsById.values()) {
      if (created >= 6) {
        break;
      }
      for (String courseCode : student.enrolledCourseCodes) {
        if (!selectedCourses.contains(courseCode)) {
          continue;
        }
        Course course = coursesByCode.get(courseCode);
        if (course == null || course.groups.size() < 2) {
          continue;
        }
        String currentGroup =
            student.groupsByCourse.getOrDefault(courseCode, course.groups.get(0).groupCode);
        String targetGroup =
            course.groups.stream()
                .map(group -> group.groupCode)
                .filter(groupCode -> !groupCode.equalsIgnoreCase(currentGroup))
                .findFirst()
                .orElse(currentGroup);
        addGroupExchange(
            courseCode,
            student.userId,
            currentGroup,
            targetGroup,
            "Imported legacy schedule overlap case.",
            "PENDING",
            "");
        created++;
        break;
      }
    }
  }

  private Map<String, StudentProfile> indexStudentsByJmbag() {
    Map<String, StudentProfile> studentsByJmbag = new LinkedHashMap<>();
    for (StudentProfile student : studentsById.values()) {
      studentsByJmbag.put(student.jmbag, student);
    }
    return studentsByJmbag;
  }

  private static String scheduleKey(
      String courseCode, String weekday, LocalTime startsAt, LocalTime endsAt, String groupCode) {
    return courseCode + "|" + weekday + "|" + startsAt + "|" + endsAt + "|" + groupCode;
  }

  private static String normalizeCode(String rawCode) {
    if (rawCode == null) {
      return "";
    }
    return rawCode.trim().toUpperCase(Locale.ROOT);
  }

  private static String fallbackCourseTitle(CourseCatalogEntry metadata, String courseCode) {
    if (metadata != null && !isBlank(metadata.title())) {
      return metadata.title().trim();
    }
    return "ISVU Course " + courseCode;
  }

  private static int deriveEcts(CourseCatalogEntry metadata) {
    if (metadata == null || isBlank(metadata.workloadRaw())) {
      return 6;
    }
    String normalized = metadata.workloadRaw().replace("\\n", "\n");
    String[] parts = normalized.split("\\s+");
    for (int index = parts.length - 1; index >= 0; index--) {
      String token = parts[index].replace(',', '.');
      try {
        double value = Double.parseDouble(token);
        int ects = (int) Math.round(value);
        if (ects >= 1 && ects <= 15) {
          return ects;
        }
      } catch (NumberFormatException ignored) {
        // Ignore non-numeric tokens in workload fields.
      }
    }
    return 6;
  }

  private static int clampYearOfStudy(int year) {
    if (year < 1) {
      return 1;
    }
    if (year > 5) {
      return 5;
    }
    return year;
  }

  private String chooseGroupCodeForEnrollment(
      Course course, String requestedGroupCode, String fallbackGroupCode) {
    String sanitized = normalizeGroupName(requestedGroupCode);
    if (!isBlank(sanitized)) {
      return sanitized;
    }
    if (!isBlank(fallbackGroupCode)) {
      return fallbackGroupCode;
    }
    return course.groups.isEmpty() ? "G1" : course.groups.get(0).groupCode;
  }

  private static String normalizeGroupName(String rawGroupName) {
    if (rawGroupName == null) {
      return "";
    }
    String trimmed = rawGroupName.trim();
    if (trimmed.isBlank()) {
      return "";
    }
    return trimmed.replaceAll("\\s+", "-").toUpperCase(Locale.ROOT);
  }

  private static String firstGroup(String groupsText) {
    if (groupsText == null || groupsText.isBlank()) {
      return "P1";
    }
    String first = groupsText.split(",", 2)[0].trim();
    String normalized = normalizeGroupName(first);
    return normalized.isBlank() ? "P1" : normalized;
  }

  private static String groupCategory(String groupCode) {
    String normalized = groupCode.toUpperCase(Locale.ROOT);
    if (normalized.contains("DEMOSI") || normalized.startsWith("D")) {
      return "demosi";
    }
    return "klasicno";
  }

  private void createUser(
      long userId, String username, String fullName, PortalRole role, String jmbag) {
    usersByUsername.put(
        username.toLowerCase(Locale.ROOT),
        new PortalUser(
            userId,
            username.toLowerCase(Locale.ROOT),
            passwordEncoder.encode(DEFAULT_PASSWORD),
            fullName,
            role,
            jmbag,
            new LinkedHashSet<>()));
  }

  private void enrollSeedStudent(long userId, String courseCode, String groupCode) {
    StudentProfile student = studentsById.get(userId);
    if (student == null) {
      return;
    }
    student.enrolledCourseCodes.add(courseCode);
    student.groupsByCourse.put(courseCode, groupCode);

    PortalUser user = userById(userId);
    user.enrolledCourseCodes.add(courseCode);

    Course course = coursesByCode.get(courseCode);
    if (course != null) {
      course.enrolledStudentIds.put(userId, groupCode);
    }
  }

  private void addSchedule(
      String courseCode,
      String type,
      String groupCode,
      String category,
      String room,
      String weekday,
      String startsAt,
      String endsAt,
      String instructor) {
    scheduleById.put(
        scheduleIdSequence.incrementAndGet(),
        new ScheduleEvent(
            scheduleIdSequence.get(),
            courseCode,
            type,
            groupCode,
            category,
            room,
            weekday,
            LocalTime.parse(startsAt),
            LocalTime.parse(endsAt),
            instructor));
  }

  private void addPoint(
      String courseCode,
      long studentId,
      String component,
      double points,
      double maxPoints,
      boolean published,
      String enteredBy) {
    pointsById.put(
        pointIdSequence.incrementAndGet(),
        new PointEntry(
            pointIdSequence.get(),
            courseCode,
            studentId,
            component,
            points,
            maxPoints,
            published,
            enteredBy));
  }

  private void addExam(
      String courseCode,
      String title,
      LocalDateTime dateTime,
      String room,
      int capacity,
      boolean published) {
    examsById.put(
        examIdSequence.incrementAndGet(),
        new ExamEvent(
            examIdSequence.get(),
            courseCode,
            title,
            dateTime,
            room,
            capacity,
            published,
            countEnrolledStudents(courseCode)));
  }

  private void addGroupExchange(
      String courseCode,
      long studentId,
      String fromGroup,
      String toGroup,
      String reason,
      String status,
      String decidedBy) {
    groupExchangesById.put(
        exchangeIdSequence.incrementAndGet(),
        new GroupExchangeRequest(
            exchangeIdSequence.get(),
            courseCode,
            studentId,
            fromGroup,
            toGroup,
            status,
            reason,
            decidedBy));
  }

  public enum PortalRole {
    STUDENT,
    LECTURER,
    ASSISTANT,
    ADMIN,
    STUSLU
  }

  public record PortalSessionUser(long userId, String username, String fullName, PortalRole role)
      implements Serializable {
    private static final long serialVersionUID = 1L;
  }

  public record AuthView(
      long userId,
      String username,
      String fullName,
      String role,
      String redirectSection,
      List<String> enabledActions) {}

  public record ActionResult(String message, WorkspaceView workspace) {}

  public record WorkspaceView(
      String branding,
      String subtitle,
      AuthView me,
      List<NavigationItemView> navigation,
      List<StatCardView> dashboardCards,
      CurrentSemesterView currentSemester,
      List<SemesterView> semesters,
      List<CourseView> courses,
      List<CourseView> myCourses,
      List<StudentView> students,
      List<UserView> users,
      List<ScheduleView> calendar,
      List<ScheduleView> lectureSchedule,
      List<ScheduleView> labSchedule,
      List<PointView> points,
      List<GradeOverviewView> gradingOverview,
      List<ExamView> exams,
      List<GroupExchangeView> groupExchanges,
      List<PermissionView> permissions,
      List<SyncOperationView> syncOperations,
      List<ActivityView> recentActivity,
      List<DemoAccountView> demoAccounts,
      List<String> enabledActions) {}

  public record NavigationItemView(String id, String label, boolean enabled) {}

  public record StatCardView(String label, String value, String hint) {}

  public record CurrentSemesterView(String code, String academicYear, String term) {}

  public record SemesterView(
      String code,
      String academicYear,
      String term,
      String startDate,
      String endDate,
      boolean active) {}

  public record CourseView(
      String code,
      String name,
      int ects,
      String semesterCode,
      String lecturer,
      String assistant,
      List<GroupView> groups) {}

  public record GroupView(String groupCode, String type, String category, int capacity) {}

  public record StudentView(
      long userId,
      String jmbag,
      String fullName,
      int yearOfStudy,
      String studyProgram,
      List<String> enrolledCourses,
      Map<String, String> groups) {}

  public record UserView(
      long userId, String username, String fullName, String role, String jmbag) {}

  public record ScheduleView(
      long id,
      String courseCode,
      String type,
      String groupCode,
      String category,
      String room,
      String weekday,
      String startsAt,
      String endsAt,
      String instructor) {}

  public record PointView(
      long id,
      String courseCode,
      String component,
      String studentJmbag,
      String studentName,
      double points,
      double maxPoints,
      double percentage,
      boolean published,
      String enteredBy) {}

  public record GradeOverviewView(
      String studentJmbag,
      String studentName,
      String courseCode,
      double collectedPoints,
      double maxPoints,
      double percentage,
      int finalGrade,
      boolean fullyPublished) {}

  public record ExamView(
      long id,
      String courseCode,
      String title,
      String dateTime,
      String room,
      int capacity,
      long allocatedStudents,
      boolean published) {}

  public record GroupExchangeView(
      long id,
      String courseCode,
      String studentName,
      String studentJmbag,
      String fromGroup,
      String toGroup,
      String status,
      String reason,
      String decidedBy) {}

  public record PermissionView(
      String courseCode, String lecturer, String assistant, List<String> capabilities) {}

  public record SyncOperationView(String operation, String lastRunAt, String status) {}

  public record ActivityView(String occurredAt, String actor, String action, String details) {}

  public record DemoAccountView(String username, String password, String role, String fullName) {}

  private static final class GradeAccumulator {
    private String studentJmbag;
    private String studentName;
    private String courseCode;
    private double collected;
    private double maximum;
    private boolean published = true;

    private GradeOverviewView toView() {
      double percentage = maximum == 0 ? 0 : (collected / maximum) * 100;
      int finalGrade =
          percentage >= 90
              ? 5
              : percentage >= 75 ? 4 : percentage >= 60 ? 3 : percentage >= 50 ? 2 : 1;
      return new GradeOverviewView(
          studentJmbag,
          studentName,
          courseCode,
          round(collected),
          round(maximum),
          round(percentage),
          finalGrade,
          published);
    }

    private static double round(double value) {
      return Math.round(value * 100.0) / 100.0;
    }
  }

  private static final class PortalUser {
    private final long userId;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final PortalRole role;
    private final String jmbag;
    private final Set<String> enrolledCourseCodes;

    private PortalUser(
        long userId,
        String username,
        String passwordHash,
        String fullName,
        PortalRole role,
        String jmbag,
        Set<String> enrolledCourseCodes) {
      this.userId = userId;
      this.username = username;
      this.passwordHash = passwordHash;
      this.fullName = fullName;
      this.role = role;
      this.jmbag = jmbag;
      this.enrolledCourseCodes = enrolledCourseCodes;
    }
  }

  private static final class StudentProfile {
    private final long userId;
    private final String jmbag;
    private final String fullName;
    private final int yearOfStudy;
    private final String studyProgram;
    private final Map<String, String> groupsByCourse;
    private final Set<String> enrolledCourseCodes = new LinkedHashSet<>();

    private StudentProfile(
        long userId,
        String jmbag,
        String fullName,
        int yearOfStudy,
        String studyProgram,
        Map<String, String> groupsByCourse) {
      this.userId = userId;
      this.jmbag = jmbag;
      this.fullName = fullName;
      this.yearOfStudy = yearOfStudy;
      this.studyProgram = studyProgram;
      this.groupsByCourse = groupsByCourse;
    }
  }

  private static final class Semester {
    private final String code;
    private final String academicYear;
    private final String term;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final boolean active;

    private Semester(
        String code,
        String academicYear,
        String term,
        LocalDate startDate,
        LocalDate endDate,
        boolean active) {
      this.code = code;
      this.academicYear = academicYear;
      this.term = term;
      this.startDate = startDate;
      this.endDate = endDate;
      this.active = active;
    }

    private Semester withActive(boolean active) {
      return new Semester(code, academicYear, term, startDate, endDate, active);
    }

    private SemesterView toView() {
      return new SemesterView(
          code,
          academicYear,
          term,
          DATE_FORMAT.format(startDate),
          DATE_FORMAT.format(endDate),
          active);
    }
  }

  private static final class Course {
    private final String code;
    private final String name;
    private final String semesterCode;
    private final int ects;
    private Long lecturerUserId;
    private Long assistantUserId;
    private final List<GroupDefinition> groups;
    private final Map<Long, String> enrolledStudentIds;

    private Course(
        String code,
        String name,
        String semesterCode,
        int ects,
        Long lecturerUserId,
        Long assistantUserId,
        List<GroupDefinition> groups,
        Map<Long, String> enrolledStudentIds) {
      this.code = code;
      this.name = name;
      this.semesterCode = semesterCode;
      this.ects = ects;
      this.lecturerUserId = lecturerUserId;
      this.assistantUserId = assistantUserId;
      this.groups = groups;
      this.enrolledStudentIds = enrolledStudentIds;
    }
  }

  private static final class GroupDefinition {
    private final String groupCode;
    private final String type;
    private final String category;
    private final int capacity;

    private GroupDefinition(String groupCode, String type, String category, int capacity) {
      this.groupCode = groupCode;
      this.type = type;
      this.category = category;
      this.capacity = capacity;
    }

    private GroupView toView() {
      return new GroupView(groupCode, type, category, capacity);
    }
  }

  private static final class ScheduleEvent {
    private final long id;
    private final String courseCode;
    private final String type;
    private final String groupCode;
    private final String category;
    private final String room;
    private final String weekday;
    private final LocalTime startsAt;
    private final LocalTime endsAt;
    private final String instructor;

    private ScheduleEvent(
        long id,
        String courseCode,
        String type,
        String groupCode,
        String category,
        String room,
        String weekday,
        LocalTime startsAt,
        LocalTime endsAt,
        String instructor) {
      this.id = id;
      this.courseCode = courseCode;
      this.type = type;
      this.groupCode = groupCode;
      this.category = category;
      this.room = room;
      this.weekday = weekday;
      this.startsAt = startsAt;
      this.endsAt = endsAt;
      this.instructor = instructor;
    }
  }

  private static final class PointEntry {
    private final long id;
    private final String courseCode;
    private final long studentId;
    private final String component;
    private final double points;
    private final double maxPoints;
    private boolean published;
    private final String enteredBy;

    private PointEntry(
        long id,
        String courseCode,
        long studentId,
        String component,
        double points,
        double maxPoints,
        boolean published,
        String enteredBy) {
      this.id = id;
      this.courseCode = courseCode;
      this.studentId = studentId;
      this.component = component;
      this.points = points;
      this.maxPoints = maxPoints;
      this.published = published;
      this.enteredBy = enteredBy;
    }
  }

  private static final class ExamEvent {
    private final long id;
    private final String courseCode;
    private final String title;
    private final LocalDateTime dateTime;
    private final String room;
    private final int capacity;
    private boolean published;
    private final long allocatedStudents;

    private ExamEvent(
        long id,
        String courseCode,
        String title,
        LocalDateTime dateTime,
        String room,
        int capacity,
        boolean published,
        long allocatedStudents) {
      this.id = id;
      this.courseCode = courseCode;
      this.title = title;
      this.dateTime = dateTime;
      this.room = room;
      this.capacity = capacity;
      this.published = published;
      this.allocatedStudents = allocatedStudents;
    }
  }

  private static final class GroupExchangeRequest {
    private final long id;
    private final String courseCode;
    private final long studentId;
    private final String fromGroup;
    private final String toGroup;
    private String status;
    private final String reason;
    private String decidedBy;

    private GroupExchangeRequest(
        long id,
        String courseCode,
        long studentId,
        String fromGroup,
        String toGroup,
        String status,
        String reason,
        String decidedBy) {
      this.id = id;
      this.courseCode = courseCode;
      this.studentId = studentId;
      this.fromGroup = fromGroup;
      this.toGroup = toGroup;
      this.status = status;
      this.reason = reason;
      this.decidedBy = decidedBy;
    }
  }

  private static final class SyncOperation {
    private final String operation;
    private String lastRunAt;
    private String lastStatus;

    private SyncOperation(String operation, String lastRunAt, String lastStatus) {
      this.operation = operation;
      this.lastRunAt = lastRunAt;
      this.lastStatus = lastStatus;
    }

    private SyncOperationView toView() {
      return new SyncOperationView(operation, lastRunAt, lastStatus);
    }
  }

  private record ActivityLogEntry(String occurredAt, String actor, String action, String details) {
    private ActivityView toView() {
      return new ActivityView(occurredAt, actor, action, details);
    }
  }
}
