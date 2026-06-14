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
class DemonstratorControllerTest {

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
  void demoStudentSeesOwnDemonstratorDuty() throws Exception {
    // The seeder makes student.ana a demonstrator on her course.
    mockMvc
        .perform(get("/api/v1/academic/my/demonstratures").session(login("student.ana")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].courseCode").exists());
  }

  @Test
  void holderAssignsAndRemovesDemonstrator() throws Exception {
    MockHttpSession holder = login("lecturer.marko");
    // student.ana's JMBAG, on course 1 where marko teaches.
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/demonstrators")
                .session(holder)
                .contentType("application/json")
                .content("{\"jmbag\":\"0036500000\"}"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/academic/courses/1/demonstrators").session(holder))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.jmbag=='0036500000')]").exists());
  }

  @Test
  void studentCannotAssignDemonstrator() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/demonstrators")
                .session(login("student.ana"))
                .contentType("application/json")
                .content("{\"jmbag\":\"0036500000\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void assigningUnknownStudentReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/academic/courses/1/demonstrators")
                .session(login("admin.ferko"))
                .contentType("application/json")
                .content("{\"jmbag\":\"0000000000\"}"))
        .andExpect(status().isNotFound());
  }
}
