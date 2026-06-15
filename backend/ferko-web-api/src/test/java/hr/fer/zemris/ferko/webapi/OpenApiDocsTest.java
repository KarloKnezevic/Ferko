package hr.fer.zemris.ferko.webapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

/**
 * Smoke test guarding the API documentation surface: both the OpenAPI JSON and the Swagger UI must
 * be reachable without authentication. The Swagger UI page requires the {@code
 * springdoc-openapi-starter-webmvc-ui} dependency, so this test fails if it is dropped again.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiDocsTest {

  @Autowired private TestRestTemplate rest;

  @Test
  void openApiJsonIsServed() {
    ResponseEntity<String> response = rest.getForEntity("/v3/api-docs", String.class);
    assertEquals(200, response.getStatusCode().value());
    assertTrue(response.getBody() != null && response.getBody().contains("openapi"));
  }

  @Test
  void swaggerUiIsServed() {
    ResponseEntity<String> response = rest.getForEntity("/swagger-ui/index.html", String.class);
    assertEquals(200, response.getStatusCode().value());
  }
}
