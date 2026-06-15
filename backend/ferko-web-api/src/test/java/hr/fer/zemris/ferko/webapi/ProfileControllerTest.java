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
class ProfileControllerTest {

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
  void studentProfileIncludesAcademicDetails() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/my/profile").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("student.ana"))
        .andExpect(jsonPath("$.jmbag").exists())
        .andExpect(jsonPath("$.roles").isArray());
  }

  @Test
  void adminProfileHasRolesButNoJmbag() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(get("/api/v1/academic/my/profile").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("admin.ferko"))
        .andExpect(jsonPath("$.roles").isArray());
  }

  @Test
  void anonymousIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/academic/my/profile")).andExpect(status().isUnauthorized());
  }

  @Test
  void studentHasStudySummary() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/my/study-summary").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.enrolledCourses").isNumber())
        .andExpect(jsonPath("$.ectsEnrolled").isNumber())
        .andExpect(jsonPath("$.weightedGpa").isNumber());
  }

  @Test
  void lecturerHasTeachingLoad() throws Exception {
    MockHttpSession session = login("lecturer.marko");
    mockMvc
        .perform(get("/api/v1/academic/my/teaching-load").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.courseCount").isNumber())
        .andExpect(jsonPath("$.courses").isArray());
  }
}
