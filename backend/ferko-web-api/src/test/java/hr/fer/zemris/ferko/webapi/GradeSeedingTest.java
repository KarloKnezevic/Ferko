package hr.fer.zemris.ferko.webapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hr.fer.zemris.ferko.webapi.bootstrap.GradeSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Verifies the grade simulation populated components, points and final grades for seeded courses.
 */
@SpringBootTest
@AutoConfigureMockMvc
class GradeSeedingTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private GradeSeeder gradeSeeder;

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
  void seededCourseHasComponentsAndGrades() throws Exception {
    MockHttpSession staff = login("lecturer.marko");
    mockMvc
        .perform(get("/api/v1/academic/courses/1/grade-components").session(staff))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.shortName=='ZI')]").exists());
    mockMvc
        .perform(get("/api/v1/academic/courses/1/grades").session(staff))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].finalGrade").isNumber());
  }

  @Test
  void demoStudentSeesSimulatedGrade() throws Exception {
    mockMvc
        .perform(get("/api/v1/academic/my/grades").session(login("student.ana")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].finalGrade").isNumber());
  }

  @Test
  void reSeedingIsIdempotent() throws Exception {
    MockHttpSession staff = login("lecturer.marko");
    int before = componentCount(staff);
    // Re-running the seeder must not add components to an already-graded course.
    gradeSeeder.run(null);
    org.junit.jupiter.api.Assertions.assertEquals(before, componentCount(staff));
  }

  private int componentCount(MockHttpSession session) throws Exception {
    String body =
        mockMvc
            .perform(get("/api/v1/academic/courses/1/grade-components").session(session))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    return new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).size();
  }
}
