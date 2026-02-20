package hr.fer.zemris.ferko.webapi;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class PortalControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void studentLoginAndWorkspaceExposeAcademicData() throws Exception {
    MockHttpSession session = login("student.ana", "ferko123");

    mockMvc
        .perform(get("/api/v1/portal/workspace").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.me.role").value("STUDENT"))
        .andExpect(jsonPath("$.currentSemester.code").value("2025Z"))
        .andExpect(jsonPath("$.myCourses[0].code").exists())
        .andExpect(jsonPath("$.gradingOverview[0].studentJmbag").value("0036501001"))
        .andExpect(jsonPath("$.navigation[0].id").value("dashboard"));
  }

  @Test
  void invalidLoginReturnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/portal/auth/login")
                .contentType("application/json")
                .content(
                    """
                    {
                      "username": "student.ana",
                      "password": "wrong-pass"
                    }
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error", containsString("Invalid username or password")));
  }

  @Test
  void workspaceRequiresSessionLogin() throws Exception {
    mockMvc
        .perform(get("/api/v1/portal/workspace"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error", containsString("Login is required")));
  }

  @Test
  void adminCanCreateSemesterButStudentCannot() throws Exception {
    MockHttpSession adminSession = login("admin.ferko", "ferko123");

    mockMvc
        .perform(
            post("/api/v1/portal/actions")
                .session(adminSession)
                .contentType("application/json")
                .content(
                    """
                    {
                      "type": "CREATE_SEMESTER",
                      "payload": {
                        "code": "2026L",
                        "academicYear": "2025/2026",
                        "term": "summer",
                        "startDate": "2026-02-23",
                        "endDate": "2026-06-26"
                      }
                    }
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message", containsString("2026L")))
        .andExpect(jsonPath("$.workspace.semesters[?(@.code=='2026L')]").isNotEmpty());

    MockHttpSession studentSession = login("student.ana", "ferko123");

    mockMvc
        .perform(
            post("/api/v1/portal/actions")
                .session(studentSession)
                .contentType("application/json")
                .content(
                    """
                    {
                      "type": "CREATE_SEMESTER",
                      "payload": {
                        "code": "2026X",
                        "academicYear": "2026/2027",
                        "term": "winter",
                        "startDate": "2026-10-05",
                        "endDate": "2027-02-12"
                      }
                    }
                    """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error", containsString("not allowed for role STUDENT")));
  }

  private MockHttpSession login(String username, String password) throws Exception {
    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/v1/portal/auth/login")
                    .contentType("application/json")
                    .content(
                        """
                        {
                          "username": "%s",
                          "password": "%s"
                        }
                        """
                            .formatted(username, password)))
            .andExpect(status().isOk())
            .andReturn();

    return (MockHttpSession) loginResult.getRequest().getSession(false);
  }
}
