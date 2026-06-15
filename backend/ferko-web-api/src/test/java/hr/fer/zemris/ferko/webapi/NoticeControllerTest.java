package hr.fer.zemris.ferko.webapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  private long publishNotice(MockHttpSession session, String json) throws Exception {
    String body =
        mockMvc
            .perform(
                post("/api/v1/academic/notices")
                    .session(session)
                    .contentType("application/json")
                    .content(json))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
    if (!matcher.find()) {
      throw new IllegalStateException("no id in response: " + body);
    }
    return Long.parseLong(matcher.group(1));
  }

  @Test
  void adminCanDeleteANotice() throws Exception {
    MockHttpSession admin = login("admin.ferko");
    long id = publishNotice(admin, "{\"title\":\"Briši me\",\"body\":\"x\",\"pinned\":false}");
    mockMvc
        .perform(delete("/api/v1/academic/notices/" + id).session(admin))
        .andExpect(status().isNoContent());
  }

  @Test
  void lecturerDeletesOwnCourseNoticeButNotFacultyWide() throws Exception {
    MockHttpSession lecturer = login("lecturer.marko");
    long courseNotice =
        publishNotice(
            lecturer, "{\"courseId\":1,\"title\":\"Kolegij\",\"body\":\"x\",\"pinned\":false}");
    long facultyNotice =
        publishNotice(
            login("admin.ferko"), "{\"title\":\"Fakultet\",\"body\":\"x\",\"pinned\":false}");

    mockMvc
        .perform(delete("/api/v1/academic/notices/" + facultyNotice).session(lecturer))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(delete("/api/v1/academic/notices/" + courseNotice).session(lecturer))
        .andExpect(status().isNoContent());
  }

  @Test
  void studentCannotDeleteANotice() throws Exception {
    long id =
        publishNotice(
            login("admin.ferko"), "{\"title\":\"Fakultet\",\"body\":\"x\",\"pinned\":false}");
    mockMvc
        .perform(delete("/api/v1/academic/notices/" + id).session(login("student.ana")))
        .andExpect(status().isForbidden());
  }

  @Test
  void deletingMissingNoticeReturns404() throws Exception {
    mockMvc
        .perform(delete("/api/v1/academic/notices/999999").session(login("admin.ferko")))
        .andExpect(status().isNotFound());
  }
}
