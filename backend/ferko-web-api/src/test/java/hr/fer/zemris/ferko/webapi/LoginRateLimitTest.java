package hr.fer.zemris.ferko.webapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "ferko.security.login-rate-limit.enabled=true",
      "ferko.security.login-rate-limit.max-attempts=3",
      "ferko.security.login-rate-limit.window-seconds=60"
    })
class LoginRateLimitTest {

  @Autowired private TestRestTemplate rest;

  @Test
  void blocksLoginAttemptsBeyondTheWindowLimit() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request =
        new HttpEntity<>("{\"username\":\"nepostojeci\",\"password\":\"krivo\"}", headers);

    boolean sawTooMany = false;
    int lastStatus = 0;
    for (int i = 0; i < 5; i++) {
      ResponseEntity<String> response =
          rest.exchange("/api/v1/auth/login", HttpMethod.POST, request, String.class);
      lastStatus = response.getStatusCode().value();
      if (lastStatus == 429) {
        sawTooMany = true;
        break;
      }
    }

    assertTrue(sawTooMany, "expected HTTP 429 after exceeding the attempt limit");
    assertEquals(429, lastStatus);
  }
}
