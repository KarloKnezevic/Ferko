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
class FlagControllerTest {

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
  void evaluatesPrerequisiteExpression() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    mockMvc
        .perform(
            post("/api/v1/academic/flags/evaluate")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"expression\":\"present(\\\"MI1\\\") && points(\\\"MI1\\\") >= 10\","
                        + "\"presentExams\":[\"MI1\"],\"points\":{\"MI1\":12.0}}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value(true));
  }

  @Test
  void rejectsMalformedExpression() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    mockMvc
        .perform(
            post("/api/v1/academic/flags/evaluate")
                .session(session)
                .contentType("application/json")
                .content("{\"expression\":\"bogus(\\\"X\\\")\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void studentCannotEvaluateFlags() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/flags/evaluate")
                .session(session)
                .contentType("application/json")
                .content("{\"expression\":\"true\"}"))
        .andExpect(status().isForbidden());
  }
}
