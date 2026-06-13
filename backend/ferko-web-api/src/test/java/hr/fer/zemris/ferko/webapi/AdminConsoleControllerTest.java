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
class AdminConsoleControllerTest {

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
  void adminListsUsersWithRoles() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(get("/api/v1/academic/users").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].username").exists())
        .andExpect(jsonPath("$[0].roles").isArray());
  }

  @Test
  void studentCannotListUsers() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/users").session(session))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminCreatesSemester() throws Exception {
    MockHttpSession session = login("admin.ferko");
    mockMvc
        .perform(
            post("/api/v1/academic/semesters")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"code\":\"2026L\",\"academicYear\":\"2025/2026\",\"term\":\"LJETNI\","
                        + "\"startsOn\":\"2026-03-01\",\"endsOn\":\"2026-07-15\",\"active\":false}"))
        .andExpect(status().isCreated());
  }

  @Test
  void studentCannotCreateSemester() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/semesters")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"code\":\"2026L\",\"academicYear\":\"2025/2026\",\"term\":\"LJETNI\","
                        + "\"startsOn\":\"2026-03-01\",\"endsOn\":\"2026-07-15\",\"active\":false}"))
        .andExpect(status().isForbidden());
  }
}
