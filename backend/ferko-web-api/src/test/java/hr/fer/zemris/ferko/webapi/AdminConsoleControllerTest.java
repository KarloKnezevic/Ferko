package hr.fer.zemris.ferko.webapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AdminConsoleControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper json = new ObjectMapper();

  private long userId(MockHttpSession adminSession, String username) throws Exception {
    String body =
        mockMvc
            .perform(get("/api/v1/academic/users").session(adminSession))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    for (JsonNode user : json.readTree(body)) {
      if (username.equals(user.get("username").asText())) {
        return user.get("id").asLong();
      }
    }
    throw new IllegalStateException("Seeded user not found: " + username);
  }

  private MockHttpSession login(String username) throws Exception {
    MockHttpSession session = new MockHttpSession();
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"" + username + "\",\"password\":\"ferko123\"}"))
        .andExpect(status().isOk());
    return session;
  }

  @Test
  void adminListsUsersWithRoles() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(get("/api/v1/academic/users").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].username").exists())
        .andExpect(jsonPath("$[0].roles").isArray());
  }

  @Test
  void studentCannotListUsers() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/users").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminSeesSettingsWithoutSecrets() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(get("/api/v1/academic/settings").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.grading.excellent").value(88))
        .andExpect(jsonPath("$.grading.sufficient").value(50))
        .andExpect(jsonPath("$.scheduler.defaultPopulationSize").isNumber())
        .andExpect(jsonPath("$.security.jwtHmacSecretConfigured").isBoolean())
        // The actual secret must never be serialised.
        .andExpect(jsonPath("$.security.hmacSecret").doesNotExist());
  }

  @Test
  void studentCannotSeeSettings() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/settings").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminSeesSyncStatusCounts() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(get("/api/v1/academic/sync/status").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.courses").isNumber())
        .andExpect(jsonPath("$.students").isNumber());
  }

  @Test
  void studentCannotSeeSyncStatus() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/sync/status").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCreatesSemester() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(
            post("/api/v1/academic/semesters")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"code\":\"2026L\",\"academicYear\":\"2025/2026\",\"term\":\"LJETNI\","
                        + "\"startsOn\":\"2026-03-01\",\"endsOn\":\"2026-07-15\",\"active\":false}"))
        .andExpect(status().isCreated());
  }

  @Test
  void studentCannotCreateSemester() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/semesters")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"code\":\"2026L\",\"academicYear\":\"2025/2026\",\"term\":\"LJETNI\","
                        + "\"startsOn\":\"2026-03-01\",\"endsOn\":\"2026-07-15\",\"active\":false}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void lecturerAssignsStaffToCourse() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/staff")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"assistant.iva\",\"role\":\"ASISTENT\"}"))
        .andExpect(status().isNoContent());
  }

  @Test
  void assigningUnknownUserReturnsNotFound() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/staff")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"ne.postoji\",\"role\":\"ASISTENT\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void studentCannotAssignStaff() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/staff")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"assistant.iva\",\"role\":\"ASISTENT\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void assigningUnknownStudentToGroupReturnsNotFound() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/group-assignments")
                .session(session)
                .contentType("application/json")
                .content("{\"jmbag\":\"0000000000\",\"groupId\":1}"))
        .andExpect(status().isNotFound());
  }

  @Test
  void studentCannotAssignGroups() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/group-assignments")
                .session(session)
                .contentType("application/json")
                .content("{\"jmbag\":\"0036000001\",\"groupId\":1}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCreateSemesterIsAuditedAndVisible() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(
            post("/api/v1/academic/semesters")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"code\":\"2027Z\",\"academicYear\":\"2027/2028\",\"term\":\"ZIMSKI\","
                        + "\"startsOn\":\"2027-10-01\",\"endsOn\":\"2028-02-15\",\"active\":false}"))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/v1/academic/audit?limit=50").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.action=='SEMESTER_CREATED' && @.entityId=='2027Z')]").exists())
        .andExpect(jsonPath("$[?(@.entityId=='2027Z')].actor").value("admin.ferko"));
  }

  @Test
  void studentCannotSeeAuditTrail() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/audit").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminSeesStudentProfileWithCoursesAndSchedule() throws Exception {
    MockHttpSession session = login("admin.ferko");
    long id = userId(session, "student.ana");
    mockMvc
        .perform(get("/api/v1/academic/users/" + id + "/profile").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("student.ana"))
        .andExpect(jsonPath("$.student").value(true))
        .andExpect(jsonPath("$.roles").isArray())
        .andExpect(jsonPath("$.courses").isArray())
        .andExpect(jsonPath("$.weekly").isArray());
  }

  @Test
  void studentCannotSeeUserProfile() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/users/1/profile").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminResetsPasswordAndUserCanLogInWithTheNewOne() throws Exception {
    MockHttpSession admin = login("admin.ferko");
    // Reset an enrolled student (username = JMBAG, all digits). No test ever logs in as one of
    // these, so mutating its password cannot contaminate other tests sharing the seeded database.
    EnrolledStudent target = anEnrolledStudent(admin);
    String body =
        mockMvc
            .perform(
                post("/api/v1/academic/users/" + target.id() + "/reset-password").session(admin))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value(target.username()))
            .andExpect(jsonPath("$.temporaryPassword").isString())
            .andReturn()
            .getResponse()
            .getContentAsString();
    String temporary = json.readTree(body).get("temporaryPassword").asText();

    // The reset truly took effect: the user can authenticate with the new one-time password.
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .session(new MockHttpSession())
                .contentType("application/json")
                .content(
                    "{\"username\":\""
                        + target.username()
                        + "\",\"password\":\""
                        + temporary
                        + "\"}"))
        .andExpect(status().isOk());
  }

  private record EnrolledStudent(long id, String username) {}

  private EnrolledStudent anEnrolledStudent(MockHttpSession adminSession) throws Exception {
    String body =
        mockMvc
            .perform(get("/api/v1/academic/users").session(adminSession))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    for (JsonNode user : json.readTree(body)) {
      String username = user.get("username").asText();
      if (username.matches("\\d+")) {
        return new EnrolledStudent(user.get("id").asLong(), username);
      }
    }
    throw new IllegalStateException("No enrolled (JMBAG) student found in seeded data");
  }

  @Test
  void studentCannotResetPasswords() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(post("/api/v1/academic/users/1/reset-password").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentCannotSeeCourseRoster() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/courses/1/enrollments").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void staffSeesCourseRoster() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    mockMvc
        .perform(get("/api/v1/academic/courses/1/enrollments").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }
}
