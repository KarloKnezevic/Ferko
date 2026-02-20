package hr.fer.zemris.ferko.webapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PingControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void pingEndpointReturnsExpectedPayload() throws Exception {
    mockMvc
        .perform(get("/api/v1/ping"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"))
        .andExpect(jsonPath("$.message").value("ferko-modernization-phase1"));
  }

  @Test
  void landingPageIsPublicAndServesModernUi() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));

    mockMvc
        .perform(get("/index.html"))
        .andExpect(status().isOk())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("FERKO Academic Portal")));
  }
}
