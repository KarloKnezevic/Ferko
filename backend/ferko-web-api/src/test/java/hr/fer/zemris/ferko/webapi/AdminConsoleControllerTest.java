package hr.fer.zemris.ferko.webapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
