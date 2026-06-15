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
class TimetableControllerTest {

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
  void anyAuthenticatedUserSeesWeeklyTimetable() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/timetable").session(login("student.ana")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void adminSeesCollisionReport() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/timetable/collisions").session(login("admin.ferko")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalSlots").isNumber())
        .andExpect(jsonPath("$.roomConflicts").isNumber());
  }

  @Test
  void studentCannotSeeCollisionReport() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/timetable/collisions").session(login("student.ana")))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminGeneratesTimetableForCourses() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/timetable/generate")
                .session(login("admin.ferko"))
                .contentType("application/json")
                .content("{\"courseIds\":[1,2],\"periods\":6,\"algorithm\":\"GENETIC\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.courses").value(2))
        .andExpect(jsonPath("$.periods").value(6))
        .andExpect(jsonPath("$.assignments").isArray());
  }

  @Test
  void studentCannotGenerateTimetable() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/timetable/generate")
                .session(login("student.ana"))
                .contentType("application/json")
                .content("{\"courseIds\":[1,2],\"periods\":6}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminComparesAlgorithms() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/timetable/compare")
                .session(login("admin.ferko"))
                .contentType("application/json")
                .content("{\"courseIds\":[1,2,3],\"periods\":6}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.runs").isArray())
        .andExpect(jsonPath("$.runs[0].algorithm").exists());
  }

  @Test
  void adminAppliesGeneratedTimetable() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/timetable/apply")
                .session(login("admin.ferko"))
                .contentType("application/json")
                .content("{\"courseIds\":[1,2],\"periods\":6,\"algorithm\":\"GENETIC\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.courses").value(2))
        .andExpect(jsonPath("$.slotsWritten").value(2));
  }

  @Test
  void studentCannotApplyTimetable() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/timetable/apply")
                .session(login("student.ana"))
                .contentType("application/json")
                .content("{\"courseIds\":[1,2],\"periods\":6}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentCannotCompareAlgorithms() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/timetable/compare")
                .session(login("student.ana"))
                .contentType("application/json")
                .content("{\"courseIds\":[1,2],\"periods\":6}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void collisionReportIncludesRoomUtilization() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/timetable/collisions").session(login("admin.ferko")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roomUtilization").isArray());
  }

  @Test
  void adminSeesResolutionReport() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/timetable/resolution").session(login("admin.ferko")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalSlots").isNumber())
        .andExpect(jsonPath("$.conflictFree").isBoolean())
        .andExpect(jsonPath("$.collisions").isArray());
  }

  @Test
  void studentCannotSeeResolutionReport() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/timetable/resolution").session(login("student.ana")))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentCannotAutoResolve() throws Exception {
    mockMvc
        .perform(post("/api/v1/academic/timetable/resolution/auto").session(login("student.ana")))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentCannotGenerateFacultyTimetable() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/timetable/resolution/generate").session(login("student.ana")))
        .andExpect(status().isForbidden());
  }
}
