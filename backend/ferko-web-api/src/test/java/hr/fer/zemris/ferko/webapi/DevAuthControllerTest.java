package hr.fer.zemris.ferko.webapi;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(
    properties = {
      "ferko.security.dev-token.enabled=true",
      "ferko.security.jwt.hmac-secret=ferko-test-hmac-secret-0123456789abcdef"
    })
@AutoConfigureMockMvc
class DevAuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void devTokenEndpointIssuesUsableToken() throws Exception {
    MvcResult tokenResult =
        mockMvc
            .perform(
                post("/api/v1/dev/token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "userId": 101,
                          "scope": "todo.read todo.write"
                        }
                        """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.authorizationHeader", startsWith("Bearer ")))
            .andReturn();

    JsonNode tokenResponse = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
    String authorizationHeader = tokenResponse.get("authorizationHeader").asText();

    mockMvc
        .perform(get("/api/v1/todo/my").header(HttpHeaders.AUTHORIZATION, authorizationHeader))
        .andExpect(status().isOk());
  }

  @Test
  void devTokenEndpointValidatesUserId() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/dev/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "userId": 0
                    }
                    """))
        .andExpect(status().isBadRequest());
  }
}
