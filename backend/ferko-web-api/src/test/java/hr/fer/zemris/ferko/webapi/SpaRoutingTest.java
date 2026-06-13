package hr.fer.zemris.ferko.webapi;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SpaRoutingTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void rootServesTheSpaShell() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
  }

  @Test
  void clientRoutesFallBackToIndex() throws Exception {
    mockMvc
        .perform(get("/kolegiji/5"))
        .andExpect(status().isOk())
        .andExpect(forwardedUrl("/index.html"));
    mockMvc
        .perform(get("/prostorije"))
        .andExpect(status().isOk())
        .andExpect(forwardedUrl("/index.html"));
  }

  @Test
  void unknownApiPathsAreNotForwardedToTheSpa() throws Exception {
    mockMvc.perform(get("/api/v1/ne-postoji")).andExpect(status().isNotFound());
  }
}
