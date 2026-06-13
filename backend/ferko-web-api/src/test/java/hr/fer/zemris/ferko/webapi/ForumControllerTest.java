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
class ForumControllerTest {

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
  void studentPostsQuestionAndItAppears() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/2/forum")
                .session(session)
                .contentType("application/json")
                .content("{\"body\":\"Kako se prijaviti na ispit?\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber());

    mockMvc
        .perform(get("/api/v1/academic/courses/2/forum").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].body").value("Kako se prijaviti na ispit?"));
  }

  @Test
  void emptyBodyIsRejected() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/2/forum")
                .session(session)
                .contentType("application/json")
                .content("{\"body\":\"   \"}"))
        .andExpect(status().isBadRequest());
  }
}
