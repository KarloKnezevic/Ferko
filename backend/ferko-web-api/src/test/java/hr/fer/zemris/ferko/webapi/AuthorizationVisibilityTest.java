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

/**
 * Verifies the role-based and row-level visibility rules: staff see management data, students are
 * limited to their own scope, and course content is gated by enrollment/teaching relationship.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationVisibilityTest {

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
  void studentCannotBrowseStudentRoster() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/students").session(login("student.ana")))
        .andExpect(status().isForbidden());
  }

  @Test
  void staffCanBrowseStudentRoster() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/students").session(login("lecturer.marko")))
        .andExpect(status().isOk());
  }

  @Test
  void studentCannotSeeCourseGradebook() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/courses/1/points-overview").session(session))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(get("/api/v1/academic/courses/1/grades").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void teachingStaffSeeCourseGradebook() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/courses/1/points-overview").session(login("lecturer.marko")))
        .andExpect(status().isOk());
  }

  @Test
  void studentCannotSeeExamSeatingOrInvigilators() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/exams/1/seating").session(session))
        .andExpect(status().isForbidden());
    mockMvc
        .perform(get("/api/v1/academic/exams/1/assistants").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentSeesOnlyEnrolledCoursesInListing() throws Exception {
    // Seeded: student.ana is enrolled in course 1 only.
    mockMvc
        .perform(get("/api/v1/academic/courses").session(login("student.ana")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == 1)]").exists())
        .andExpect(jsonPath("$[?(@.id == 2)]").doesNotExist());
  }

  @Test
  void studentCannotListRooms() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/rooms").session(login("student.ana")))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentReachesDetailOfEnrolledCourseButNotOthers() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc.perform(get("/api/v1/academic/courses/1").session(session)).andExpect(status().isOk());
    mockMvc
        .perform(get("/api/v1/academic/courses/2").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void studentReachesFilesOfEnrolledCourseButNotOthers() throws Exception {
    MockHttpSession session = login("student.ana");
    // Seeded: student.ana is enrolled in course 1 only.
    mockMvc
        .perform(get("/api/v1/academic/courses/1/files").session(session))
        .andExpect(status().isOk());
    mockMvc
        .perform(get("/api/v1/academic/courses/2/files").session(session))
        .andExpect(status().isForbidden());
  }
}
