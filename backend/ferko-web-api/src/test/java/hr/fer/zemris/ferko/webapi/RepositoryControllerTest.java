package hr.fer.zemris.ferko.webapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class RepositoryControllerTest {

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
  void teachingStaffUploadsAndDownloads() throws Exception {
    MockHttpSession staff = login("lecturer.marko");
    byte[] bytes = "FERKO skripta".getBytes(StandardCharsets.UTF_8);
    MockMultipartFile file = new MockMultipartFile("file", "skripta.txt", "text/plain", bytes);

    MvcResult uploaded =
        mockMvc
            .perform(multipart("/api/v1/academic/courses/5/files").file(file).session(staff))
            .andExpect(status().isCreated())
            .andReturn();
    long id = mapper.readTree(uploaded.getResponse().getContentAsString()).get("id").asLong();

    // Teaching staff (course holder) may list and download the course's files.
    mockMvc
        .perform(get("/api/v1/academic/courses/5/files").session(staff))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].filename").value("skripta.txt"));
    mockMvc
        .perform(get("/api/v1/academic/files/" + id + "/download").session(staff))
        .andExpect(status().isOk())
        .andExpect(content().bytes(bytes));
  }

  @Test
  void studentNotEnrolledCannotDownloadCourseFile() throws Exception {
    MockHttpSession staff = login("lecturer.marko");
    MockMultipartFile file =
        new MockMultipartFile("file", "tajna.txt", "text/plain", new byte[] {9, 8, 7});
    MvcResult uploaded =
        mockMvc
            .perform(multipart("/api/v1/academic/courses/5/files").file(file).session(staff))
            .andExpect(status().isCreated())
            .andReturn();
    long id = mapper.readTree(uploaded.getResponse().getContentAsString()).get("id").asLong();

    // student.ana is enrolled in course 1 only, so course 5 material is off-limits.
    MockHttpSession student = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/courses/5/files").session(student))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(get("/api/v1/academic/files/" + id + "/download").session(student))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentCannotUpload() throws Exception {
    MockHttpSession student = login("student.ana");
    MockMultipartFile file =
        new MockMultipartFile("file", "x.txt", "text/plain", new byte[] {1, 2, 3});
    mockMvc
        .perform(multipart("/api/v1/academic/courses/5/files").file(file).session(student))
        .andExpect(status().isForbidden());
  }

  @Test
  void downloadMissingReturnsNotFound() throws Exception {
    MockHttpSession student = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/files/999999/download").session(student))
        .andExpect(status().isNotFound());
  }
}
