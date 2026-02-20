package hr.fer.zemris.ferko.webapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ToDoOpenApiContractTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void openApiSpecIncludesToDoPathsSecurityAndSchemas() throws Exception {
    MvcResult jsonResult =
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()).andReturn();

    String openApiJson = jsonResult.getResponse().getContentAsString();
    JsonNode spec = objectMapper.readTree(openApiJson);

    assertTrue(spec.path("openapi").asText().startsWith("3."));
    assertTrue(spec.path("paths").has("/api/v1/todo/tasks"));
    assertTrue(spec.path("paths").has("/api/v1/todo/my"));
    assertTrue(spec.path("paths").has("/api/v1/todo/assigned"));
    assertTrue(spec.path("paths").has("/api/v1/todo/tasks/{taskId}/close"));

    JsonNode createTaskOperation = spec.path("paths").path("/api/v1/todo/tasks").path("post");
    assertEquals("Create ToDo task", createTaskOperation.path("summary").asText());
    assertTrue(createTaskOperation.path("responses").has("201"));
    assertTrue(containsSecurityRequirement(createTaskOperation.path("security"), "bearerAuth"));
    assertEquals(
        "#/components/schemas/ErrorResponse", responseSchemaRef(createTaskOperation, "401"));

    JsonNode closeTaskOperation =
        spec.path("paths").path("/api/v1/todo/tasks/{taskId}/close").path("post");
    assertTrue(closeTaskOperation.path("responses").has("200"));
    assertTrue(closeTaskOperation.path("responses").has("403"));
    assertTrue(containsSecurityRequirement(closeTaskOperation.path("security"), "bearerAuth"));
    assertEquals(
        "#/components/schemas/ErrorResponse", responseSchemaRef(closeTaskOperation, "403"));

    JsonNode securityScheme = spec.path("components").path("securitySchemes").path("bearerAuth");
    assertEquals("http", securityScheme.path("type").asText());
    assertEquals("bearer", securityScheme.path("scheme").asText());
    assertEquals("JWT", securityScheme.path("bearerFormat").asText());

    JsonNode pingOperation = spec.path("paths").path("/api/v1/ping").path("get");
    JsonNode pingSecurity = pingOperation.path("security");
    assertTrue(pingSecurity.isMissingNode() || pingSecurity.isEmpty());

    MvcResult yamlResult =
        mockMvc.perform(get("/v3/api-docs.yaml")).andExpect(status().isOk()).andReturn();
    String openApiYaml = yamlResult.getResponse().getContentAsString();

    writeOpenApiArtifacts(openApiJson, openApiYaml);
  }

  private static boolean containsSecurityRequirement(JsonNode security, String requirementName) {
    if (!security.isArray()) {
      return false;
    }
    for (JsonNode securityEntry : security) {
      if (securityEntry.has(requirementName)) {
        return true;
      }
    }
    return false;
  }

  private static String responseSchemaRef(JsonNode operation, String statusCode) {
    return operation
        .path("responses")
        .path(statusCode)
        .path("content")
        .path("*/*")
        .path("schema")
        .path("$ref")
        .asText();
  }

  private static void writeOpenApiArtifacts(String openApiJson, String openApiYaml)
      throws IOException {
    Path outputDirectory = Path.of("target", "openapi");
    Files.createDirectories(outputDirectory);
    Files.writeString(outputDirectory.resolve("openapi.json"), openApiJson, StandardCharsets.UTF_8);
    Files.writeString(outputDirectory.resolve("openapi.yaml"), openApiYaml, StandardCharsets.UTF_8);
  }
}
