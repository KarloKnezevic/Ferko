package hr.fer.zemris.ferko.webapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

@SpringBootTest
@AutoConfigureMockMvc
class PortfolioControllerTest {

  @Autowired private MockMvc mockMvc;
  private final ObjectMapper json = new ObjectMapper();

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
  void userManagesOwnPortfolio() throws Exception {
    MockHttpSession session = login("student.ana");
    String body =
        mockMvc
            .perform(
                post("/api/v1/academic/my/portfolio")
                    .session(session)
                    .contentType("application/json")
                    .content(
                        "{\"title\":\"Završni rad\",\"description\":\"opis\","
                            + "\"category\":\"PROJEKT\",\"link\":\"http://x\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn()
            .getResponse()
            .getContentAsString();
    long id = json.readTree(body).get("id").asLong();

    mockMvc
        .perform(get("/api/v1/academic/my/portfolio").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.title=='Završni rad')]").exists());

    mockMvc
        .perform(delete("/api/v1/academic/my/portfolio/" + id).session(session))
        .andExpect(status().isNoContent());
  }

  @Test
  void blankTitleIsRejected() throws Exception {
    MockHttpSession session = login("student.ana");
    mockMvc
        .perform(
            post("/api/v1/academic/my/portfolio")
                .session(session)
                .contentType("application/json")
                .content("{\"title\":\"  \",\"description\":\"\",\"category\":\"\",\"link\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void anonymousIsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/v1/academic/my/portfolio")).andExpect(status().isUnauthorized());
  }
}
