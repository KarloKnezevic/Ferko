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
class AcademicControllerTest {

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
  void seededAcademicDataIsExposed() throws Exception {
    MockHttpSession session = login("admin.ferko");

    mockMvc
        .perform(get("/api/v1/academic/semesters/active").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("2026LJ"))
        .andExpect(jsonPath("$.active").value(true));

    mockMvc
        .perform(get("/api/v1/academic/courses").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].code").exists());

    mockMvc
        .perform(get("/api/v1/academic/rooms").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.code == 'D272')]").exists());

    mockMvc.perform(get("/api/v1/academic/students").session(session)).andExpect(status().isOk());
  }

  @Test
  void adminCanCreateCourse() throws Exception {
    MockHttpSession session = login("admin.ferko");

    mockMvc
        .perform(
            post("/api/v1/academic/courses")
                .session(session)
                .contentType("application/json")
                .content(
                    "{\"code\":\"TST-API\",\"name\":\"Testni kolegij\",\"ects\":5,"
                        + "\"description\":\"opis\",\"literature\":\"lit\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.code").value("TST-API"))
        .andExpect(jsonPath("$.semesterCode").value("2026LJ"))
        .andExpect(jsonPath("$.id").isNumber());
  }

  @Test
  void studentCannotCreateCourse() throws Exception {
    MockHttpSession session = login("student.ana");

    mockMvc
        .perform(
            post("/api/v1/academic/courses")
                .session(session)
                .contentType("application/json")
                .content("{\"code\":\"NOPE\",\"name\":\"x\",\"ects\":5}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void academicApiRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/academic/courses")).andExpect(status().isUnauthorized());
  }
}
