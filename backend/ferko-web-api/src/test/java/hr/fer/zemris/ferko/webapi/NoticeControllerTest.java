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
class NoticeControllerTest {

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
  void seededNoticesAreListedPinnedFirst() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/notices").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].pinned").value(true));
  }

  @Test
  void staffCanPublishANotice() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    mockMvc
        .perform(
            post("/api/v1/academic/notices")
                .session(session)
                .contentType("application/json")
                .content("{\"title\":\"Test obavijest\",\"body\":\"Sadržaj\",\"pinned\":false}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber());
  }

  @Test
  void studentCannotPublishANotice() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/notices")
                .session(session)
                .contentType("application/json")
                .content("{\"title\":\"x\",\"body\":\"y\",\"pinned\":false}"))
        .andExpect(status().isForbidden());
  }
}
