package hr.fer.zemris.ferko.webapi.portal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Comparator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class FerkoPortalServiceTest {

  private static final String DEFAULT_PASSWORD = "ferko123";
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Test
  void authenticateAndFindUserCoverHappyAndErrorPaths() {
    FerkoPortalService service = new FerkoPortalService();

    FerkoPortalService.AuthView auth =
        service.authenticate("student.ana", DEFAULT_PASSWORD).orElseThrow();
    assertEquals("student.ana", auth.username());
    assertEquals("STUDENT", auth.role());

    assertTrue(service.authenticate("student.ana", "wrong-password").isEmpty());
    assertTrue(service.authenticate("", DEFAULT_PASSWORD).isEmpty());

    FerkoPortalService.AuthView found = service.findUserById(auth.userId());
    assertEquals(auth.userId(), found.userId());
    assertEquals("dashboard", found.redirectSection());

    ResponseStatusException ex =
        assertThrows(ResponseStatusException.class, () -> service.findUserById(-1L));
    assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
  }

  @Test
  void buildWorkspaceIsRoleAwareAcrossAllPortalRoles() {
    FerkoPortalService service = new FerkoPortalService();

    FerkoPortalService.WorkspaceView studentWorkspace =
        service.buildWorkspace(sessionUser(service, "student.ana"));
    assertEquals("STUDENT", studentWorkspace.me().role());
    assertFalse(studentWorkspace.myCourses().isEmpty());
    assertEquals(3, studentWorkspace.dashboardCards().size());
    assertTrue(
        studentWorkspace.navigation().stream()
            .anyMatch(item -> "administration".equals(item.id()) && !item.enabled()));

    FerkoPortalService.WorkspaceView lecturerWorkspace =
        service.buildWorkspace(sessionUser(service, "lecturer.marko"));
    assertEquals("LECTURER", lecturerWorkspace.me().role());
    assertFalse(lecturerWorkspace.myCourses().isEmpty());
    assertEquals(1, lecturerWorkspace.users().size());

    FerkoPortalService.WorkspaceView assistantWorkspace =
        service.buildWorkspace(sessionUser(service, "assistant.iva"));
    assertEquals("ASSISTANT", assistantWorkspace.me().role());
    assertFalse(assistantWorkspace.myCourses().isEmpty());
    assertFalse(assistantWorkspace.labSchedule().isEmpty());

    FerkoPortalService.WorkspaceView adminWorkspace =
        service.buildWorkspace(sessionUser(service, "admin.ferko"));
    assertEquals("ADMIN", adminWorkspace.me().role());
    assertTrue(adminWorkspace.enabledActions().contains("CREATE_SEMESTER"));
    assertTrue(adminWorkspace.users().size() > 1);

    FerkoPortalService.WorkspaceView stusluWorkspace =
        service.buildWorkspace(sessionUser(service, "stuslu.sara"));
    assertEquals("STUSLU", stusluWorkspace.me().role());
    assertTrue(stusluWorkspace.enabledActions().contains("ENROLL_STUDENT"));
    assertTrue(
        stusluWorkspace.navigation().stream()
            .anyMatch(item -> "points".equals(item.id()) && !item.enabled()));
  }

  @Test
  void adminActionsCoverSemesterCourseSyncAndEnrollmentFlows() {
    FerkoPortalService service = new FerkoPortalService();
    FerkoPortalService.PortalSessionUser admin = sessionUser(service, "admin.ferko");

    FerkoPortalService.ActionResult createSemesterResult =
        service.executeAction(
            admin,
            "CREATE_SEMESTER",
            json(
                """
                {
                  "code": "2026L",
                  "academicYear": "2025/2026",
                  "term": "summer",
                  "startDate": "2026-02-23",
                  "endDate": "2026-06-26"
                }
                """));
    assertTrue(
        createSemesterResult.workspace().semesters().stream()
            .anyMatch(semester -> "2026L".equals(semester.code())));

    FerkoPortalService.ActionResult activateSemesterResult =
        service.executeAction(admin, "ACTIVATE_SEMESTER", json("{\"code\": \"2026L\"}"));
    assertEquals("2026L", activateSemesterResult.workspace().currentSemester().code());

    FerkoPortalService.ActionResult createCourseResult =
        service.executeAction(
            admin,
            "CREATE_COURSE",
            json(
                """
                {
                  "code": "AI1",
                  "name": "Applied AI",
                  "semesterCode": "2026L",
                  "ects": 6,
                  "lecturerUsername": "lecturer.marko",
                  "assistantUsername": "assistant.iva"
                }
                """));
    assertTrue(
        createCourseResult.workspace().courses().stream()
            .anyMatch(course -> "AI1".equals(course.code())));

    service.executeAction(
        admin,
        "ASSIGN_STAFF",
        json(
            """
            {
              "courseCode": "AI1",
              "lecturerUsername": "lecturer.marko",
              "assistantUsername": "assistant.iva"
            }
            """));

    FerkoPortalService.ActionResult defineGroupResult =
        service.executeAction(
            admin,
            "DEFINE_GROUP",
            json(
                """
                {
                  "courseCode": "AI1",
                  "groupCode": "LAB-X",
                  "type": "LAB",
                  "category": "klasicno",
                  "capacity": 25
                }
                """));
    assertTrue(
        defineGroupResult.workspace().courses().stream()
            .filter(course -> "AI1".equals(course.code()))
            .flatMap(course -> course.groups().stream())
            .anyMatch(group -> "LAB-X".equals(group.groupCode())));

    FerkoPortalService.ActionResult importResult =
        service.executeAction(
            admin,
            "IMPORT_STUDENTS",
            json(
                """
                {
                  "students": [
                    {"jmbag": "0036501999", "fullName": "Petra Test", "yearOfStudy": 3},
                    {"jmbag": "0036501001", "fullName": "Ana Horvat", "yearOfStudy": 2}
                  ]
                }
                """));
    assertTrue(importResult.message().contains("1 student(s) imported."));

    service.executeAction(admin, "SYNC_STUDENTS", json("{}"));
    service.executeAction(admin, "SYNC_ROOMS", json("{}"));
    service.executeAction(admin, "SYNC_LECTURES", json("{}"));
    service.executeAction(admin, "SYNC_LABS", json("{}"));

    service.executeAction(
        admin,
        "ENROLL_STUDENT",
        json(
            """
            {
              "studentJmbag": "0036501999",
              "courseCode": "AI1"
            }
            """));

    FerkoPortalService.ActionResult assignGroupResult =
        service.executeAction(
            admin,
            "ASSIGN_STUDENT_GROUP",
            json(
                """
                {
                  "studentJmbag": "0036501999",
                  "courseCode": "AI1",
                  "groupCode": "LAB-X"
                }
                """));
    assertTrue(
        assignGroupResult.workspace().students().stream()
            .filter(student -> "0036501999".equals(student.jmbag()))
            .anyMatch(student -> "LAB-X".equals(student.groups().get("AI1"))));

    FerkoPortalService.ActionResult createExamResult =
        service.executeAction(
            admin,
            "CREATE_EXAM",
            json(
                """
                {
                  "courseCode": "AI1",
                  "title": "Midterm AI1",
                  "dateTime": "2026-03-02T09:00:00",
                  "room": "A-201",
                  "capacity": 60
                }
                """));
    long createdExamId =
        createExamResult.workspace().exams().stream()
            .filter(exam -> "AI1".equals(exam.courseCode()))
            .max(Comparator.comparingLong(FerkoPortalService.ExamView::id))
            .orElseThrow()
            .id();

    FerkoPortalService.ActionResult publishExamResult =
        service.executeAction(
            admin, "PUBLISH_EXAM_RESULTS", json("{\"examId\": %d}".formatted(createdExamId)));
    assertTrue(
        publishExamResult.workspace().exams().stream()
            .anyMatch(exam -> exam.id() == createdExamId && exam.published()));
  }

  @Test
  void lecturerAndAssistantCanManagePointsAndExamPublishing() {
    FerkoPortalService service = new FerkoPortalService();
    FerkoPortalService.PortalSessionUser lecturer = sessionUser(service, "lecturer.marko");
    FerkoPortalService.PortalSessionUser assistant = sessionUser(service, "assistant.iva");

    FerkoPortalService.ActionResult enterPointsResult =
        service.executeAction(
            assistant,
            "ENTER_POINTS",
            json(
                """
                {
                  "courseCode": "UTR",
                  "studentJmbag": "0036501001",
                  "component": "Lab retake",
                  "points": 19,
                  "maxPoints": 20
                }
                """));
    assertTrue(
        enterPointsResult.workspace().points().stream()
            .anyMatch(
                point ->
                    "UTR".equals(point.courseCode())
                        && "Lab retake".equals(point.component())
                        && "0036501001".equals(point.studentJmbag())));

    FerkoPortalService.ActionResult publishPointsResult =
        service.executeAction(lecturer, "PUBLISH_POINTS", json("{\"courseCode\": \"UTR\"}"));
    assertTrue(
        publishPointsResult.workspace().points().stream()
            .filter(point -> "UTR".equals(point.courseCode()))
            .allMatch(FerkoPortalService.PointView::published));

    FerkoPortalService.ActionResult createExamResult =
        service.executeAction(
            lecturer,
            "CREATE_EXAM",
            json(
                """
                {
                  "courseCode": "UTR",
                  "title": "Additional oral exam",
                  "dateTime": "2026-02-10T12:00:00",
                  "room": "A-111",
                  "capacity": 30
                }
                """));
    long examId =
        createExamResult.workspace().exams().stream()
            .filter(exam -> "Additional oral exam".equals(exam.title()))
            .findFirst()
            .orElseThrow()
            .id();

    FerkoPortalService.ActionResult publishExamResult =
        service.executeAction(
            lecturer, "PUBLISH_EXAM_RESULTS", json("{\"examId\": %d}".formatted(examId)));
    assertTrue(
        publishExamResult.workspace().exams().stream()
            .anyMatch(exam -> exam.id() == examId && exam.published()));

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () ->
            service.executeAction(
                assistant,
                "ENTER_POINTS",
                json(
                    """
                    {
                      "courseCode": "UTR",
                      "studentJmbag": "0036501001",
                      "component": "Invalid",
                      "points": 40,
                      "maxPoints": 20
                    }
                    """)));
  }

  @Test
  void exchangeFlowSupportsStudentRequestAndAdministrativeDecision() {
    FerkoPortalService service = new FerkoPortalService();
    FerkoPortalService.PortalSessionUser student = sessionUser(service, "student.ana");
    FerkoPortalService.PortalSessionUser admin = sessionUser(service, "admin.ferko");
    FerkoPortalService.PortalSessionUser stuslu = sessionUser(service, "stuslu.sara");

    FerkoPortalService.ActionResult requestResult =
        service.executeAction(
            student,
            "REQUEST_GROUP_EXCHANGE",
            json(
                """
                {
                  "courseCode": "UTR",
                  "fromGroup": "L1",
                  "toGroup": "L2",
                  "reason": "Schedule overlap"
                }
                """));
    long requestedId =
        requestResult.workspace().groupExchanges().stream()
            .filter(exchange -> "0036501001".equals(exchange.studentJmbag()))
            .max(Comparator.comparingLong(FerkoPortalService.GroupExchangeView::id))
            .orElseThrow()
            .id();

    FerkoPortalService.ActionResult approveResult =
        service.executeAction(
            admin,
            "DECIDE_GROUP_EXCHANGE",
            json("{\"exchangeId\": %d, \"decision\": \"APPROVED\"}".formatted(requestedId)));
    assertTrue(
        approveResult.workspace().groupExchanges().stream()
            .anyMatch(
                exchange -> exchange.id() == requestedId && "APPROVED".equals(exchange.status())));

    FerkoPortalService.ActionResult secondRequestResult =
        service.executeAction(
            student,
            "REQUEST_GROUP_EXCHANGE",
            json(
                """
                {
                  "courseCode": "UTR",
                  "fromGroup": "L1",
                  "toGroup": "D1",
                  "reason": "Need later session"
                }
                """));
    long secondRequestedId =
        secondRequestResult.workspace().groupExchanges().stream()
            .filter(exchange -> "0036501001".equals(exchange.studentJmbag()))
            .max(Comparator.comparingLong(FerkoPortalService.GroupExchangeView::id))
            .orElseThrow()
            .id();

    FerkoPortalService.ActionResult rejectResult =
        service.executeAction(
            stuslu,
            "DECIDE_GROUP_EXCHANGE",
            json("{\"exchangeId\": %d, \"decision\": \"REJECTED\"}".formatted(secondRequestedId)));
    assertTrue(
        rejectResult.workspace().groupExchanges().stream()
            .anyMatch(
                exchange ->
                    exchange.id() == secondRequestedId && "REJECTED".equals(exchange.status())));
  }

  @Test
  void executeActionValidatesPermissionsAndPayloadErrors() {
    FerkoPortalService service = new FerkoPortalService();
    FerkoPortalService.PortalSessionUser student = sessionUser(service, "student.ana");
    FerkoPortalService.PortalSessionUser assistant = sessionUser(service, "assistant.iva");
    FerkoPortalService.PortalSessionUser admin = sessionUser(service, "admin.ferko");

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () -> service.executeAction(admin, "UNSUPPORTED_ACTION", json("{}")));
    assertStatus(HttpStatus.BAD_REQUEST, () -> service.executeAction(admin, " ", json("{}")));
    assertStatus(
        HttpStatus.FORBIDDEN,
        () -> service.executeAction(student, "CREATE_SEMESTER", json("{\"code\": \"2027Z\"}")));
    assertStatus(
        HttpStatus.FORBIDDEN,
        () -> service.executeAction(assistant, "DEFINE_GROUP", json("{\"courseCode\": \"UTR\"}")));

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () ->
            service.executeAction(
                admin,
                "CREATE_SEMESTER",
                json(
                    """
                    {
                      "code": "2025Z",
                      "academicYear": "2025/2026",
                      "term": "winter",
                      "startDate": "2025-10-06",
                      "endDate": "2026-02-13"
                    }
                    """)));
    assertStatus(
        HttpStatus.NOT_FOUND,
        () -> service.executeAction(admin, "ACTIVATE_SEMESTER", json("{\"code\": \"2099X\"}")));
    assertStatus(
        HttpStatus.BAD_REQUEST,
        () ->
            service.executeAction(
                admin,
                "CREATE_COURSE",
                json(
                    """
                    {
                      "code": "NEW1",
                      "name": "Invalid semester",
                      "semesterCode": "2099X"
                    }
                    """)));
    assertStatus(
        HttpStatus.BAD_REQUEST,
        () ->
            service.executeAction(
                admin,
                "IMPORT_STUDENTS",
                json(
                    """
                    {
                      "students": []
                    }
                    """)));
    assertStatus(
        HttpStatus.NOT_FOUND,
        () ->
            service.executeAction(
                student,
                "REQUEST_GROUP_EXCHANGE",
                json(
                    """
                    {
                      "courseCode": "XXX",
                      "fromGroup": "A",
                      "toGroup": "B"
                    }
                    """)));
    assertStatus(
        HttpStatus.BAD_REQUEST,
        () ->
            service.executeAction(
                admin,
                "DECIDE_GROUP_EXCHANGE",
                json(
                    """
                    {
                      "exchangeId": 2501,
                      "decision": "MAYBE"
                    }
                    """)));

    FerkoPortalService.ActionResult createCourse =
        service.executeAction(
            admin,
            "CREATE_COURSE",
            json(
                """
                {
                  "code": "QA1",
                  "name": "Quality Assurance",
                  "semesterCode": "2025Z"
                }
                """));
    assertNotNull(createCourse.workspace());

    assertStatus(
        HttpStatus.BAD_REQUEST,
        () ->
            service.executeAction(
                admin,
                "ASSIGN_STUDENT_GROUP",
                json(
                    """
                    {
                      "studentJmbag": "0036501001",
                      "courseCode": "QA1",
                      "groupCode": "UNKNOWN"
                    }
                    """)));
    assertStatus(
        HttpStatus.BAD_REQUEST,
        () ->
            service.executeAction(
                admin,
                "CREATE_SEMESTER",
                json(
                    """
                    {
                      "code": "2027L",
                      "academicYear": "2026/2027",
                      "term": "summer",
                      "startDate": "2027/02/23",
                      "endDate": "2027-06-26"
                    }
                    """)));
  }

  private static FerkoPortalService.PortalSessionUser sessionUser(
      FerkoPortalService service, String username) {
    FerkoPortalService.AuthView auth =
        service.authenticate(username, DEFAULT_PASSWORD).orElseThrow();
    return new FerkoPortalService.PortalSessionUser(
        auth.userId(),
        auth.username(),
        auth.fullName(),
        FerkoPortalService.PortalRole.valueOf(auth.role()));
  }

  private static void assertStatus(HttpStatus status, Executable executable) {
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, executable);
    assertEquals(status, exception.getStatusCode());
  }

  private static JsonNode json(String raw) {
    try {
      return OBJECT_MAPPER.readTree(raw);
    } catch (IOException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
