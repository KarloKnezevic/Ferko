package hr.fer.zemris.ferko.webapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class StudentExamControllerTest {

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
  void studentSeesOwnExams() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/my/exams").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void nonStudentGetsEmptyList() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(get("/api/v1/academic/my/exams").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void anonymousIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/academic/my/exams")).andExpect(status().isUnauthorized());
  }

  @Test
  void studentRegistersAndUnregistersForExam() throws Exception {
    // Lecturer defines an exam on the first course (student.ana is enrolled in course 1).
    MockHttpSession lecturer = login("lecturer.marko");
    String examBody =
        mockMvc
            .perform(
                post("/api/v1/academic/courses/1/exams")
                    .session(lecturer)
                    .contentType("application/json")
                    .content(
                        "{\"title\":\"Prijava test\",\"shortName\":\"PT\","
                            + "\"kind\":\"MEDJUISPIT\",\"durationMinutes\":60,\"maxPoints\":10.0}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long examId = new ObjectMapper().readTree(examBody).get("id").asLong();

    MockHttpSession student = login("student.ana");
    mockMvc
        .perform(post("/api/v1/academic/my/exams/" + examId + "/registration").session(student))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(delete("/api/v1/academic/my/exams/" + examId + "/registration").session(student))
        .andExpect(status().isNoContent());
  }

  @Test
  void registeringForMissingExamIsNotFound() throws Exception {
    MockHttpSession student = login("student.ana");
    mockMvc
        .perform(post("/api/v1/academic/my/exams/999999/registration").session(student))
        .andExpect(status().isNotFound());
  }
}
