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
class ConsultationControllerTest {

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
  void lecturerAddsAndAnyoneReadsConsultations() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/consultations")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"dayOfWeek\":\"Ponedjeljak\",\"startsAt\":\"10:00\","
                        + "\"endsAt\":\"11:30\",\"location\":\"C-04\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber());

    mockMvc
        .perform(get("/api/v1/academic/courses/1/consultations").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.location=='C-04')]").exists());
  }

  @Test
  void invalidTimeIsRejected() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/consultations")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"dayOfWeek\":\"Pon\",\"startsAt\":\"11:00\","
                        + "\"endsAt\":\"10:00\",\"location\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void studentCannotAddConsultations() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/consultations")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"dayOfWeek\":\"Pon\",\"startsAt\":\"10:00\","
                        + "\"endsAt\":\"11:00\",\"location\":\"x\"}"))
        .andExpect(status().isForbidden());
  }
}
