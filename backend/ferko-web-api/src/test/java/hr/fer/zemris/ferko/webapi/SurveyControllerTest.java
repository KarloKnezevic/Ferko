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
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SurveyControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper mapper = new ObjectMapper();

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
  void staffCreatesSurveyStudentSubmitsAndStaffReadsResults() throws Exception {
    MockHttpSession staff = login("lecturer.marko");
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/academic/courses/1/surveys")
                    .session(staff)
                    .contentType("application/json")
                    .content("{\"title\":\"Evaluacija\",\"questions\":[\"Jasnoća\",\"Tempo\"]}"))
            .andExpect(status().isCreated())
            .andReturn();
    long surveyId = mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

    // Read the survey's questions back as a student.
    MockHttpSession student = login("student.ana");
    MvcResult list =
        mockMvc
            .perform(get("/api/v1/academic/courses/1/surveys").session(student))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode questions =
        mapper.readTree(list.getResponse().getContentAsString()).get(0).get("questions");
    long q1 = questions.get(0).get("id").asLong();

    mockMvc
        .perform(
            post("/api/v1/academic/surveys/" + surveyId + "/responses")
                .session(student)
                .contentType("application/json")
                .content("{\"answers\":[{\"questionId\":" + q1 + ",\"rating\":5}]}"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/academic/surveys/" + surveyId + "/results").session(staff))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].responses").value(1));
  }

  @Test
  void studentCannotCreateSurvey() throws Exception {
    MockHttpSession student = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/surveys")
                .session(student)
                .contentType("application/json")
                .content("{\"title\":\"x\",\"questions\":[\"a\"]}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void invalidRatingIsRejected() throws Exception {
    MockHttpSession staff = login("lecturer.marko");
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/academic/courses/1/surveys")
                    .session(staff)
                    .contentType("application/json")
                    .content("{\"title\":\"E2\",\"questions\":[\"Q\"]}"))
            .andExpect(status().isCreated())
            .andReturn();
    long surveyId = mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
    long q =
        mapper
            .readTree(
                mockMvc
                    .perform(
                        get("/api/v1/academic/surveys/" + surveyId + "/results").session(staff))
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get(0)
            .get("questionId")
            .asLong();

    MockHttpSession student = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/surveys/" + surveyId + "/responses")
                .session(student)
                .contentType("application/json")
                .content("{\"answers\":[{\"questionId\":" + q + ",\"rating\":9}]}"))
        .andExpect(status().isBadRequest());
  }
}
