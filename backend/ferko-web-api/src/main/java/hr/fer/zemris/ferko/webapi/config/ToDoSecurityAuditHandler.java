package hr.fer.zemris.ferko.webapi.config;

import hr.fer.zemris.ferko.application.port.ToDoAuditAction;
import hr.fer.zemris.ferko.application.port.ToDoAuditEvent;
import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import hr.fer.zemris.ferko.application.port.ToDoAuditOutcome;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

public class ToDoSecurityAuditHandler implements AccessDeniedHandler, AuthenticationEntryPoint {

  private static final Pattern CLOSE_TASK_PATH_PATTERN =
      Pattern.compile("^/api/v1/todo/tasks/(\\d+)/close$");

  private final ToDoAuditLogPort auditLogPort;
  private final AccessDeniedHandler deniedDelegate = new BearerTokenAccessDeniedHandler();
  private final AuthenticationEntryPoint unauthenticatedDelegate =
      new BearerTokenAuthenticationEntryPoint();

  public ToDoSecurityAuditHandler(ToDoAuditLogPort auditLogPort) {
    this.auditLogPort = auditLogPort;
  }

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      AccessDeniedException accessDeniedException)
      throws IOException, ServletException {
    logDeniedAttempt(request, "security-access-denied", accessDeniedException.getMessage());
    deniedDelegate.handle(request, response, accessDeniedException);
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authenticationException)
      throws IOException, ServletException {
    logDeniedAttempt(request, "security-unauthenticated", authenticationException.getMessage());
    unauthenticatedDelegate.commence(request, response, authenticationException);
  }

  private void logDeniedAttempt(HttpServletRequest request, String category, String reason) {
    ResolvedTarget target = resolveTarget(request);
    if (target == null) {
      return;
    }
    try {
      auditLogPort.log(
          new ToDoAuditEvent(
              target.action(),
              ToDoAuditOutcome.DENIED,
              resolveActorUserId(request.getUserPrincipal()),
              target.taskId(),
              category + ": " + (reason == null ? "" : reason)));
    } catch (RuntimeException ignored) {
      // Security failure handling must never fail closed because of audit persistence issues.
    }
  }

  private static ResolvedTarget resolveTarget(HttpServletRequest request) {
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      return null;
    }
    String path = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (!contextPath.isBlank() && path.startsWith(contextPath)) {
      path = path.substring(contextPath.length());
    }
    if ("/api/v1/todo/tasks".equals(path)) {
      return new ResolvedTarget(ToDoAuditAction.CREATE, null);
    }
    Matcher matcher = CLOSE_TASK_PATH_PATTERN.matcher(path);
    if (matcher.matches()) {
      return new ResolvedTarget(ToDoAuditAction.CLOSE, Long.valueOf(matcher.group(1)));
    }
    return null;
  }

  private static Long resolveActorUserId(Principal principal) {
    if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
      return null;
    }
    try {
      return Long.valueOf(principal.getName());
    } catch (NumberFormatException ex) {
      return null;
    }
  }

  private record ResolvedTarget(ToDoAuditAction action, Long taskId) {}
}
