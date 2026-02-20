package hr.fer.zemris.ferko.webapi;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ToDoControllerTest {

  private static final String TODO_READ_SCOPE = "todo.read";
  private static final String TODO_WRITE_SCOPE = "todo.write";
  private static final String TODO_READ_WRITE_SCOPE = TODO_READ_SCOPE + " " + TODO_WRITE_SCOPE;
  private static final String TEST_JWT_SECRET = "ferko-test-hmac-secret-0123456789abcdef";

  @Autowired private MockMvc mockMvc;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void createAndListEndpointsReturnOpenTask() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/todo/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "assigneeId": 22,
                      "title": "Prepare architecture extraction",
                      "description": "Capture ToDo migration baseline.",
                      "deadline": "2026-03-20T10:15:00",
                      "priority": "MEDIUM"
                    }
                    """)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(11L)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.ownerId").value(11))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.status").value("OPEN"));

    mockMvc
        .perform(get("/api/v1/todo/my").header(HttpHeaders.AUTHORIZATION, bearerToken(22L)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("Prepare architecture extraction"))
        .andExpect(jsonPath("$[0].status").value("OPEN"));

    mockMvc
        .perform(get("/api/v1/todo/assigned").header(HttpHeaders.AUTHORIZATION, bearerToken(11L)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].assigneeId").value(22));
  }

  @Test
  void closeEndpointRejectsUnrelatedActor() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/todo/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "assigneeId": 41,
                      "title": "Document close policy",
                      "description": "",
                      "deadline": "2026-04-10T09:00:00",
                      "priority": "TRIVIAL"
                    }
                    """)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(31L)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/todo/tasks/{taskId}/close", 1)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(999L)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error", containsString("cannot manage task")));
  }

  @Test
  void closeEndpointClosesTaskForAssignee() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/todo/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "assigneeId": 61,
                      "title": "Close me",
                      "description": "Task to be closed by assignee.",
                      "deadline": "2026-05-01T08:00:00",
                      "priority": "CRITICAL"
                    }
                    """)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(51L)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/todo/tasks/{taskId}/close", 1)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(61L)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CLOSED"));

    mockMvc
        .perform(get("/api/v1/todo/my").header(HttpHeaders.AUTHORIZATION, bearerToken(61L)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void endpointsRequireAuthenticatedPrincipal() throws Exception {
    mockMvc.perform(get("/api/v1/todo/my")).andExpect(status().isUnauthorized());
  }

  @Test
  void endpointsRejectNonNumericSubjectClaim() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/todo/my")
                .header(HttpHeaders.AUTHORIZATION, bearerToken("not-a-numeric-user-id")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createEndpointRequiresWriteScope() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/todo/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "assigneeId": 22,
                      "title": "No write scope",
                      "description": "Should be forbidden.",
                      "deadline": "2026-03-20T10:15:00",
                      "priority": "MEDIUM"
                    }
                    """)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(11L, TODO_READ_SCOPE, List.of())))
        .andExpect(status().isForbidden());

    Integer count =
        jdbcTemplate.queryForObject("select count(*) from todo_audit_log", Integer.class);
    assertEquals(1, count);
    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select action, outcome, actor_user_id, task_id from todo_audit_log");
    assertEquals("CREATE", row.get("action"));
    assertEquals("DENIED", row.get("outcome"));
    assertEquals(11L, ((Number) row.get("actor_user_id")).longValue());
    assertNull(row.get("task_id"));
  }

  @Test
  void listEndpointRequiresReadScope() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/todo/my")
                .header(HttpHeaders.AUTHORIZATION, bearerToken(11L, TODO_WRITE_SCOPE, List.of())))
        .andExpect(status().isForbidden());
  }

  @Test
  void closeEndpointDeniedBySecurityIsAudited() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/todo/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "assigneeId": 41,
                      "title": "Close denied by policy",
                      "description": "",
                      "deadline": "2026-04-10T09:00:00",
                      "priority": "TRIVIAL"
                    }
                    """)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(31L)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/todo/tasks/{taskId}/close", 1)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(999L, TODO_READ_SCOPE, List.of())))
        .andExpect(status().isForbidden());

    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            """
            select action, outcome, actor_user_id, task_id
            from todo_audit_log
            where outcome = 'DENIED'
            order by id desc
            limit 1
            """);
    assertEquals("CLOSE", row.get("action"));
    assertEquals("DENIED", row.get("outcome"));
    assertEquals(999L, ((Number) row.get("actor_user_id")).longValue());
    assertEquals(1L, ((Number) row.get("task_id")).longValue());
  }

  @Test
  void unauthenticatedCreateAttemptIsAudited() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/todo/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "assigneeId": 22,
                      "title": "Unauthenticated write",
                      "description": "Should be audited as denied.",
                      "deadline": "2026-03-20T10:15:00",
                      "priority": "MEDIUM"
                    }
                    """))
        .andExpect(status().isUnauthorized());

    Integer count =
        jdbcTemplate.queryForObject("select count(*) from todo_audit_log", Integer.class);
    assertEquals(1, count);
    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select action, outcome, actor_user_id, task_id from todo_audit_log");
    assertEquals("CREATE", row.get("action"));
    assertEquals("DENIED", row.get("outcome"));
    assertNull(row.get("actor_user_id"));
    assertNull(row.get("task_id"));
  }

  @Test
  void createEndpointAllowsMappedRoleWithoutScope() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/todo/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "assigneeId": 22,
                      "title": "Role based access",
                      "description": "Allowed by TODO_WRITE role.",
                      "deadline": "2026-03-20T10:15:00",
                      "priority": "MEDIUM"
                    }
                    """)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    bearerToken(11L, "", List.of("TODO_WRITE", "todo_read"))))
        .andExpect(status().isCreated());
  }

  @Test
  void createEndpointPersistsAuditLogEntry() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/todo/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "assigneeId": 22,
                      "title": "Persist audit",
                      "description": "Create audit test.",
                      "deadline": "2026-03-20T10:15:00",
                      "priority": "MEDIUM"
                    }
                    """)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(11L)))
        .andExpect(status().isCreated());

    Integer count =
        jdbcTemplate.queryForObject("select count(*) from todo_audit_log", Integer.class);
    assertEquals(1, count);
    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            "select action, outcome, actor_user_id, task_id from todo_audit_log");
    assertEquals("CREATE", row.get("action"));
    assertEquals("SUCCESS", row.get("outcome"));
    assertEquals(11L, ((Number) row.get("actor_user_id")).longValue());
    assertEquals(1L, ((Number) row.get("task_id")).longValue());
  }

  @Test
  void closeDeniedAttemptPersistsAuditLogEntry() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/todo/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "assigneeId": 41,
                      "title": "Denied close",
                      "description": "",
                      "deadline": "2026-04-10T09:00:00",
                      "priority": "TRIVIAL"
                    }
                    """)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(31L)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/todo/tasks/{taskId}/close", 1)
                .header(HttpHeaders.AUTHORIZATION, bearerToken(999L)))
        .andExpect(status().isForbidden());

    Integer count =
        jdbcTemplate.queryForObject("select count(*) from todo_audit_log", Integer.class);
    assertEquals(2, count);
    Map<String, Object> row =
        jdbcTemplate.queryForMap(
            """
            select action, outcome, actor_user_id, task_id
            from todo_audit_log
            where action = 'CLOSE'
            order by id desc
            limit 1
            """);
    assertEquals("CLOSE", row.get("action"));
    assertEquals("DENIED", row.get("outcome"));
    assertEquals(999L, ((Number) row.get("actor_user_id")).longValue());
    assertEquals(1L, ((Number) row.get("task_id")).longValue());
  }

  private static String bearerToken(long userId) throws JOSEException {
    return bearerToken(userId, TODO_READ_WRITE_SCOPE, List.of());
  }

  private static String bearerToken(String subject) throws JOSEException {
    return bearerToken(subject, TODO_READ_WRITE_SCOPE, List.of());
  }

  private static String bearerToken(long userId, String scopes, List<String> roles)
      throws JOSEException {
    return bearerToken(String.valueOf(userId), scopes, roles);
  }

  private static String bearerToken(String subject, String scopes, List<String> roles)
      throws JOSEException {
    Instant now = Instant.now();
    JWTClaimsSet.Builder claimsBuilder =
        new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer("ferko-test-suite")
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(900)));
    if (!scopes.isBlank()) {
      claimsBuilder.claim("scope", scopes);
    }
    if (!roles.isEmpty()) {
      claimsBuilder.claim("roles", roles);
    }
    JWTClaimsSet claims = claimsBuilder.build();
    SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
    JWSSigner signer = new MACSigner(TEST_JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    jwt.sign(signer);
    return "Bearer " + jwt.serialize();
  }
}
