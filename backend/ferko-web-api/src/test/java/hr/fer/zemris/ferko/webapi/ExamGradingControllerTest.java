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
class ExamGradingControllerTest {

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
  void autoGradesSubmissionsAgainstKey() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    mockMvc
        .perform(
            post("/api/v1/academic/exams/1/auto-grade")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"correctAnswers\":[\"A\",\"B\",\"A,C\"],"
                        + "\"policy\":{\"correctPoints\":1,\"incorrectPoints\":-0.2,\"blankPoints\":0},"
                        + "\"submissions\":[{\"jmbag\":\"0036500001\",\"answers\":[\"A\",\"B\",\"C\"]},"
                        + "{\"jmbag\":\"0036500002\",\"answers\":[\"A\",\"D\",\"\"]}]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].jmbag").value("0036500001"))
        .andExpect(jsonPath("$[0].correct").value(3))
        .andExpect(jsonPath("$[1].correct").value(1));
  }

  @Test
  void studentCannotAutoGrade() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/exams/1/auto-grade")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"correctAnswers\":[\"A\"],\"submissions\":[{\"jmbag\":\"x\",\"answers\":[\"A\"]}]}"))
        .andExpect(status().isForbidden());
  }
}
