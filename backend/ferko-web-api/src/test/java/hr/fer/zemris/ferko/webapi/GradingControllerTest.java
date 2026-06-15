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

    // Distinct from the seeded MI/ZI/LAB components (unique course_id + short_name).
    String comp =
        mockMvc
            .perform(
                post("/api/v1/academic/courses/" + courseId + "/grade-components")
                    .session(session)
                    .contentType("application/json")
                    .content(
                        "{\"name\":\"Kviz\",\"shortName\":\"KV\",\"maxPoints\":20,\"ordinal\":9}"))
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
        // Other students may already be graded by the seeder; assert this student's grade.
        .andExpect(
            jsonPath("$[?(@.studentId==" + studentId + ")].finalGrade")
                .value(org.hamcrest.Matchers.hasItem(4)));
  }

  @Test
  void perCourseThresholdsDefaultThenCustomAndCompute() throws Exception {
    MockHttpSession session = login("lecturer.marko");

    mockMvc
        .perform(get("/api/v1/academic/courses/1/grade-thresholds").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.excellent").value(88))
        .andExpect(jsonPath("$.custom").value(false));

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/v1/academic/courses/1/grade-thresholds")
                .session(session)
                .contentType("application/json")
                .content("{\"excellent\":90,\"veryGood\":78,\"good\":65,\"sufficient\":55}"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/academic/courses/1/grade-thresholds").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.excellent").value(90))
        .andExpect(jsonPath("$.custom").value(true));

    mockMvc
        .perform(post("/api/v1/academic/courses/1/grades/compute").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.graded").isNumber());
  }

  @Test
  void invalidThresholdsRejected() throws Exception {
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/v1/academic/courses/1/grade-thresholds")
                .session(login("lecturer.marko"))
                .contentType("application/json")
                // not strictly decreasing
                .content("{\"excellent\":60,\"veryGood\":75,\"good\":62,\"sufficient\":50}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void studentCannotSetThresholds() throws Exception {
    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                    "/api/v1/academic/courses/1/grade-thresholds")
                .session(login("student.ana"))
                .contentType("application/json")
                .content("{\"excellent\":90,\"veryGood\":78,\"good\":65,\"sufficient\":55}"))
        .andExpect(status().isForbidden());
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
