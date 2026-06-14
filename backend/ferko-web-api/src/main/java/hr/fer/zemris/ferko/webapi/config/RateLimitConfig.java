package hr.fer.zemris.ferko.webapi.config;

import hr.fer.zemris.ferko.webapi.auth.LoginRateLimitFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Registers the login rate-limit filter when {@code ferko.security.login-rate-limit.enabled=true}
 * (off by default; switched on in the production profile). Limits are read from the central {@link
 * FerkoProperties}.
 */
@Configuration
public class RateLimitConfig {

  @Bean
  @ConditionalOnProperty(name = "ferko.security.login-rate-limit.enabled", havingValue = "true")
  public FilterRegistrationBean<LoginRateLimitFilter> loginRateLimitFilter(FerkoProperties props) {
    FerkoProperties.Security.LoginRateLimit rateLimit = props.getSecurity().getLoginRateLimit();
    FilterRegistrationBean<LoginRateLimitFilter> registration =
        new FilterRegistrationBean<>(
            new LoginRateLimitFilter(rateLimit.getMaxAttempts(), rateLimit.getWindowSeconds()));
    registration.addUrlPatterns("/api/v1/auth/login");
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
