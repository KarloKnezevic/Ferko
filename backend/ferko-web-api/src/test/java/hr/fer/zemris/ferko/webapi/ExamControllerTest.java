package hr.fer.zemris.ferko.webapi;

import static org.junit.jupiter.api.Assertions.assertTrue;
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
class ExamControllerTest {

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

  private long firstId(MockHttpSession session, String path) throws Exception {
    String body =
        mockMvc
            .perform(get(path).session(session))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode array = json.readTree(body);
    return array.get(0).get("id").asLong();
  }

  @Test
  void lecturerCanRunFullExamSeatingWorkflow() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    long courseId = firstId(session, "/api/v1/academic/courses");
    long roomId = firstId(session, "/api/v1/academic/rooms");

    // Create exam.
    String examBody =
        mockMvc
            .perform(
                post("/api/v1/academic/courses/" + courseId + "/exams")
                    .session(session)
                    .contentType("application/json")
                    .content(
                        "{\"title\":\"Prvi međuispit\",\"shortName\":\"MI1\","
                            + "\"kind\":\"MEDJUISPIT\",\"durationMinutes\":90,\"maxPoints\":20.0}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long examId = json.readTree(examBody).get("id").asLong();

    // Reserve a room with ample capacity.
    mockMvc
        .perform(
            post("/api/v1/academic/exams/" + examId + "/rooms")
                .session(session)
                .contentType("application/json")
                .content("{\"roomId\":" + roomId + ",\"capacity\":500,\"requiredAssistants\":3}"))
        .andExpect(status().isNoContent());

    // Register the enrolled cohort.
    mockMvc
        .perform(
            post("/api/v1/academic/exams/" + examId + "/registrations/from-course/" + courseId)
                .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.registered").isNumber());

    // Generate seating with the genetic optimiser.
    mockMvc
        .perform(
            post("/api/v1/academic/exams/" + examId + "/seating?strategy=GENETIC").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.feasible").value(true))
        .andExpect(jsonPath("$.strategy").value("GENETIC"));

    // Inspect and publish.
    mockMvc
        .perform(get("/api/v1/academic/exams/" + examId + "/seating").session(session))
        .andExpect(status().isOk());
    mockMvc
        .perform(post("/api/v1/academic/exams/" + examId + "/publish").session(session))
        .andExpect(status().isNoContent());

    assertTrue(examId > 0);
  }

  @Test
  void lecturerCanAssignAndRemoveInvigilator() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    long courseId = firstId(session, "/api/v1/academic/courses");
    long roomId = firstId(session, "/api/v1/academic/rooms");

    String examBody =
        mockMvc
            .perform(
                post("/api/v1/academic/courses/" + courseId + "/exams")
                    .session(session)
                    .contentType("application/json")
                    .content(
                        "{\"title\":\"Dežurstvo test\",\"shortName\":\"DZ\","
                            + "\"kind\":\"MEDJUISPIT\",\"durationMinutes\":90,\"maxPoints\":20.0}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long examId = json.readTree(examBody).get("id").asLong();

    mockMvc
        .perform(
            post("/api/v1/academic/exams/" + examId + "/rooms")
                .session(session)
                .contentType("application/json")
                .content("{\"roomId\":" + roomId + ",\"capacity\":100,\"requiredAssistants\":2}"))
        .andExpect(status().isNoContent());

    // Assigning an unknown user to an exam room yields 404.
    mockMvc
        .perform(
            post("/api/v1/academic/exams/" + examId + "/rooms/" + roomId + "/assistants")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"ne.postoji\"}"))
        .andExpect(status().isNotFound());

    // Assigning a real assistant succeeds.
    mockMvc
        .perform(
            post("/api/v1/academic/exams/" + examId + "/rooms/" + roomId + "/assistants")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"assistant.iva\"}"))
        .andExpect(status().isNoContent());

    String listBody =
        mockMvc
            .perform(get("/api/v1/academic/exams/" + examId + "/assistants").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].username").value("assistant.iva"))
            .andReturn()
            .getResponse()
            .getContentAsString();
    long assignmentId = json.readTree(listBody).get(0).get("id").asLong();

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                    "/api/v1/academic/exams/" + examId + "/assistants/" + assignmentId)
                .session(session))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/academic/exams/" + examId + "/assistants").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void studentCannotAssignInvigilator() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/exams/1/rooms/1/assistants")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"assistant.iva\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentCannotCreateExam() throws Exception {
    MockHttpSession session = login("student.ana");
    long courseId = firstId(session, "/api/v1/academic/courses");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/" + courseId + "/exams")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"title\":\"x\",\"shortName\":\"x\",\"kind\":\"MEDJUISPIT\","
                        + "\"durationMinutes\":60,\"maxPoints\":10.0}"))
        .andExpect(status().isForbidden());
  }
}
