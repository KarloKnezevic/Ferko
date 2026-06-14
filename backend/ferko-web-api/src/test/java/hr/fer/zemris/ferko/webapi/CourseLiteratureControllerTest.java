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
class CourseLiteratureControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper json = new ObjectMapper();

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

  private long firstCourseId(MockHttpSession session) throws Exception {
    String body =
        mockMvc
            .perform(get("/api/v1/academic/courses").session(session))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode array = json.readTree(body);
    return array.get(0).get("id").asLong();
  }

  @Test
  void lecturerAddsAndAnyoneReadsLiterature() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    long courseId = firstCourseId(session);

    mockMvc
        .perform(
            post("/api/v1/academic/courses/" + courseId + "/literature")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"title\":\"Strukture podataka\",\"author\":\"R. Sedgewick\","
                        + "\"mandatory\":true,\"ordinal\":0}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber());

    mockMvc
        .perform(get("/api/v1/academic/courses/" + courseId + "/literature").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.title=='Strukture podataka')]").exists());
  }

  @Test
  void blankTitleIsRejected() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    long courseId = firstCourseId(session);
    mockMvc
        .perform(
            post("/api/v1/academic/courses/" + courseId + "/literature")
                .session(session)
                .contentType("application/json")
                .content("{\"title\":\"  \",\"author\":\"\",\"mandatory\":true,\"ordinal\":0}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void studentCannotAddLiterature() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/literature")
                .session(session)
                .contentType("application/json")
                .content("{\"title\":\"x\",\"author\":\"y\",\"mandatory\":false,\"ordinal\":0}"))
        .andExpect(status().isForbidden());
  }
}
