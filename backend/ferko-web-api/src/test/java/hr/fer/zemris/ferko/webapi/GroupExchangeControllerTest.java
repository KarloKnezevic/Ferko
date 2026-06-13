package hr.fer.zemris.ferko.webapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class GroupExchangeControllerTest {

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
  void studentRequestsExchangeAndStaffApproves() throws Exception {
    MockHttpSession student = login("student.ana");
    MvcResult created =
        mockMvc
            .perform(
                post("/api/v1/academic/courses/1/group-exchange")
                    .session(student)
                    .contentType("application/json")
                    .content("{\"fromGroupId\":null,\"toGroupId\":null,\"reason\":\"Posao\"}"))
            .andExpect(status().isCreated())
            .andReturn();
    long id = mapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

    mockMvc
        .perform(get("/api/v1/academic/courses/1/group-exchange").session(student))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("PENDING"));

    MockHttpSession staff = login("stuslu.sara");
    mockMvc
        .perform(
            post("/api/v1/academic/group-exchange/" + id + "/decision")
                .session(staff)
                .contentType("application/json")
                .content("{\"approve\":true}"))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/academic/courses/1/group-exchange").session(staff))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].status").value("APPROVED"));
  }

  @Test
  void studentCannotDecide() throws Exception {
    MockHttpSession student = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/group-exchange/1/decision")
                .session(student)
                .contentType("application/json")
                .content("{\"approve\":true}"))
        .andExpect(status().isForbidden());
  }
}
