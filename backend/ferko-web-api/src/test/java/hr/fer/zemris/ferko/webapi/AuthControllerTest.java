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
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void seededAdminCanLogInAndQuerySession() throws Exception {
    MockHttpSession session = new MockHttpSession();

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .session(session)
                .contentType("application/json")
                .content("{\"username\":\"admin.ferko\",\"password\":\"ferko123\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("admin.ferko"))
        .andExpect(jsonPath("$.fullName").value("Administrator Ferko"))
        .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));

    mockMvc
        .perform(get("/api/v1/auth/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.username").value("admin.ferko"));
  }

  @Test
  void lecturerCarriesAllAssignedRoles() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .session(new MockHttpSession())
                .contentType("application/json")
                .content("{\"username\":\"lecturer.marko\",\"password\":\"ferko123\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.roles", org.hamcrest.Matchers.hasItem("ROLE_NOSITELJ")))
        .andExpect(jsonPath("$.roles", org.hamcrest.Matchers.hasItem("ROLE_NASTAVNIK")));
  }

  @Test
  void invalidPasswordIsRejected() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType("application/json")
                .content("{\"username\":\"admin.ferko\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void meRequiresAnAuthenticatedSession() throws Exception {
    mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
  }
}
