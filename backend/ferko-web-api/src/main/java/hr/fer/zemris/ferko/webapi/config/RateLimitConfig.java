package hr.fer.zemris.ferko.webapi.config;

import hr.fer.zemris.ferko.webapi.auth.LoginRateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers the login rate-limit filter when {@code ferko.security.login-rate-limit.enabled=true}
 * (off by default; switched on in the production profile).
 */
@Configuration
public class RateLimitConfig {

  @Bean
  @ConditionalOnProperty(name = "ferko.security.login-rate-limit.enabled", havingValue = "true")
  public FilterRegistrationBean<LoginRateLimitFilter> loginRateLimitFilter(
      @Value("${ferko.security.login-rate-limit.max-attempts:10}") int maxAttempts,
      @Value("${ferko.security.login-rate-limit.window-seconds:60}") long windowSeconds) {
    FilterRegistrationBean<LoginRateLimitFilter> registration =
        new FilterRegistrationBean<>(new LoginRateLimitFilter(maxAttempts, windowSeconds));
    registration.addUrlPatterns("/api/v1/auth/login");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
