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
class CourseComponentControllerTest {

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
  void staffAddsVisibleComponentAndItIsListed() throws Exception {
    MockHttpSession staff = login("lecturer.marko");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/3/components")
                .session(staff)
                .contentType("application/json")
                .content(
                    "{\"title\":\"O kolegiju\",\"content\":\"Opis\",\"ordinal\":0,\"visible\":true}"))
        .andExpect(status().isCreated());

    MockHttpSession student = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/courses/3/components").session(student))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("O kolegiju"));
  }

  @Test
  void hiddenComponentIsNotListedToStudents() throws Exception {
    MockHttpSession staff = login("lecturer.marko");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/4/components")
                .session(staff)
                .contentType("application/json")
                .content(
                    "{\"title\":\"Skriveno\",\"content\":\"x\",\"ordinal\":0,\"visible\":false}"))
        .andExpect(status().isCreated());

    MockHttpSession student = login("student.ana");
    mockMvc
        .perform(get("/api/v1/academic/courses/4/components").session(student))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void studentCannotAddComponent() throws Exception {
    MockHttpSession student = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/courses/3/components")
                .session(student)
                .contentType("application/json")
                .content("{\"title\":\"x\",\"content\":\"y\",\"ordinal\":0,\"visible\":true}"))
        .andExpect(status().isForbidden());
  }
}
