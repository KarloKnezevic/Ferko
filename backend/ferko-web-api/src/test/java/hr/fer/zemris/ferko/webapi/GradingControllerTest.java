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
class GradingControllerTest {

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

  private long topEnrolledCourse(MockHttpSession session) throws Exception {
    String body =
        mockMvc
            .perform(get("/api/v1/academic/courses").session(session))
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode arr = json.readTree(body);
    long bestId = arr.get(0).get("id").asLong();
    int best = -1;
    for (JsonNode c : arr) {
      if (c.get("enrolledStudents").asInt() > best) {
        best = c.get("enrolledStudents").asInt();
        bestId = c.get("id").asLong();
      }
    }
    return bestId;
  }

  @Test
  void componentsPointsOverviewAndGrades() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    long courseId = topEnrolledCourse(session);

    String comp =
        mockMvc
            .perform(
                post("/api/v1/academic/courses/" + courseId + "/grade-components")
                    .session(session)
                    .contentType("application/json")
                    .content(
                        "{\"name\":\"Međuispit\",\"shortName\":\"MI\",\"maxPoints\":20,\"ordinal\":1}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long componentId = json.readTree(comp).get("id").asLong();

    String overview =
        mockMvc
            .perform(
                get("/api/v1/academic/courses/" + courseId + "/points-overview").session(session))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode rows = json.readTree(overview);
    org.junit.jupiter.api.Assertions.assertTrue(
        rows.size() > 0, "course should have enrolled students");
    long studentId = rows.get(0).get("studentId").asLong();

    mockMvc
        .perform(
            post("/api/v1/academic/courses/" + courseId + "/points")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"studentId\":"
                        + studentId
                        + ",\"componentId\":"
                        + componentId
                        + ",\"points\":15.0}"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            post("/api/v1/academic/courses/" + courseId + "/grades")
                .session(session)
                .contentType("application/json")
                .content("{\"studentId\":" + studentId + ",\"finalGrade\":4}"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/academic/courses/" + courseId + "/grades").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].finalGrade").value(4));
  }

  @Test
  void studentCannotEnterPoints() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/points")
                .session(session)
                .contentType("application/json")
                .content("{\"studentId\":1,\"componentId\":1,\"points\":10.0}"))
        .andExpect(status().isForbidden());
  }
}
