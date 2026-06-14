package hr.fer.zemris.ferko.webapi.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Throttles login attempts per client IP with a fixed sliding window. Disabled by default; enabled
 * for production via {@code ferko.security.login-rate-limit.enabled}. Returns HTTP 429 once the
 * window limit is exceeded.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

  private static final String LOGIN_PATH = "/api/v1/auth/login";

  private final int maxAttempts;
  private final long windowMillis;
  private final Map<String, Deque<Long>> hitsByClient = new ConcurrentHashMap<>();

  public LoginRateLimitFilter(int maxAttempts, long windowSeconds) {
    this.maxAttempts = Math.max(1, maxAttempts);
    this.windowMillis = Math.max(1, windowSeconds) * 1000L;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (!HttpMethod.POST.matches(request.getMethod())
        || !LOGIN_PATH.equals(request.getRequestURI())) {
      chain.doFilter(request, response);
      return;
    }

    long now = System.currentTimeMillis();
    Deque<Long> hits =
        hitsByClient.computeIfAbsent(request.getRemoteAddr(), key -> new ArrayDeque<>());
    boolean limited;
    synchronized (hits) {
      while (!hits.isEmpty() && now - hits.peekFirst() > windowMillis) {
        hits.pollFirst();
      }
      limited = hits.size() >= maxAttempts;
      if (!limited) {
        hits.addLast(now);
      }
    }

    if (limited) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(windowMillis / 1000L));
      response
          .getWriter()
          .write("{\"message\":\"Previše pokušaja prijave. Pokušajte ponovno kasnije.\"}");
      return;
    }
    chain.doFilter(request, response);
  }
}
