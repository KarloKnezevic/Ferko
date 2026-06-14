package hr.fer.zemris.ferko.webapi;

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
class ExamTimetableControllerTest {

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
  void adminGeneratesExamTimetableWithLegacyComparison() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/exam-timetable/generate")
                .session(login("admin.ferko"))
                .contentType("application/json")
                .content("{\"courseIds\":[1,2,3],\"slots\":8,\"algorithm\":\"GENETIC\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.exams").value(3))
        .andExpect(jsonPath("$.feasible").isBoolean())
        .andExpect(jsonPath("$.resultConflicts").isNumber())
        .andExpect(jsonPath("$.legacyConflicts").isNumber());
  }

  @Test
  void studentCannotGenerateExamTimetable() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/exam-timetable/generate")
                .session(login("student.ana"))
                .contentType("application/json")
                .content("{\"courseIds\":[1,2],\"slots\":8}"))
        .andExpect(status().isForbidden());
  }
}
