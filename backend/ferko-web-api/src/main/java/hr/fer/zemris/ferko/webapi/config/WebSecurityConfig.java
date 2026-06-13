package hr.fer.zemris.ferko.webapi.config;

import hr.fer.zemris.ferko.application.port.ToDoAuditLogPort;
import hr.fer.zemris.ferko.application.usecase.auth.LoadAuthUserUseCase;
import hr.fer.zemris.ferko.webapi.auth.FerkoUserDetailsService;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;

@Configuration
@EnableMethodSecurity
public class WebSecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public FerkoUserDetailsService ferkoUserDetailsService(LoadAuthUserUseCase loadAuthUserUseCase) {
    return new FerkoUserDetailsService(loadAuthUserUseCase);
  }

  @Bean
  public AuthenticationManager authenticationManager(
      FerkoUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(provider);
  }

  @Bean
  public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  /**
   * Session/form authentication chain for the browser-facing FERKO API. Higher precedence than the
   * JWT resource-server chain so it owns the {@code /api/v1/auth/**} surface.
   */
  @Bean
  @Order(1)
  public SecurityFilterChain webSessionSecurityFilterChain(
      HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
    http.securityMatcher("/api/v1/auth/**", "/api/v1/academic/**")
        // TODO (Faza 6): enable cookie-based CSRF tokens once the SPA is in place.
        .csrf(AbstractHttpConfigurer::disable)
        .securityContext(context -> context.securityContextRepository(securityContextRepository))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/login")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .exceptionHandling(
            handling ->
                handling.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable);
    return http.build();
  }

  @Bean
  @Order(2)
  public SecurityFilterChain apiSecurityFilterChain(
      HttpSecurity http,
      JwtAuthenticationConverter jwtAuthenticationConverter,
      ToDoSecurityAuditHandler toDoSecurityAuditHandler)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            handling ->
                handling
                    .accessDeniedHandler(toDoSecurityAuditHandler)
                    .authenticationEntryPoint(toDoSecurityAuditHandler))
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/styles.css",
                        "/app.js",
                        "/favicon.svg",
                        "/api/v1/ping",
                        "/actuator/health",
                        "/actuator/info")
                    .permitAll()
                    .requestMatchers("/api/v1/portal/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/dev/token")
                    .permitAll()
                    .requestMatchers(
                        "/v3/api-docs",
                        "/v3/api-docs.yaml",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/todo/my", "/api/v1/todo/assigned")
                    .hasAnyAuthority("SCOPE_todo.read", "ROLE_TODO_READ")
                    .requestMatchers(
                        HttpMethod.POST, "/api/v1/todo/tasks", "/api/v1/todo/tasks/*/close")
                    .hasAnyAuthority("SCOPE_todo.write", "ROLE_TODO_WRITE")
                    // Static SPA assets and client-side routes are public; the SPA itself
                    // gates views by calling the session-protected APIs.
                    .anyRequest()
                    .permitAll())
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer
                    .authenticationEntryPoint(toDoSecurityAuditHandler)
                    .accessDeniedHandler(toDoSecurityAuditHandler)
                    .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
    return http.build();
  }

  @Bean
  public ToDoSecurityAuditHandler toDoSecurityAuditHandler(ToDoAuditLogPort auditLogPort) {
    return new ToDoSecurityAuditHandler(auditLogPort);
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter(
      @Value("${ferko.security.jwt.principal-claim:sub}") String principalClaim,
      @Value("${ferko.security.jwt.roles-claim:roles}") String rolesClaimName) {
    JwtGrantedAuthoritiesConverter scopeAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setPrincipalClaimName(principalClaim);
    converter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          List<GrantedAuthority> authorities = new ArrayList<>();
          Collection<GrantedAuthority> scopeAuthorities = scopeAuthoritiesConverter.convert(jwt);
          if (scopeAuthorities != null) {
            authorities.addAll(scopeAuthorities);
          }
          authorities.addAll(extractRoleAuthorities(jwt, rolesClaimName));
          return authorities;
        });
    return converter;
  }

  @Bean
  @ConditionalOnMissingBean(JwtDecoder.class)
  public JwtDecoder jwtDecoder(
      @Value("${ferko.security.jwt.issuer-uri:}") String issuerUri,
      @Value("${ferko.security.jwt.jwk-set-uri:}") String jwkSetUri,
      @Value("${ferko.security.jwt.hmac-secret:}") String hmacSecret,
      @Value("${ferko.security.jwt.allow-hmac-decoder:true}") boolean allowHmacDecoder) {
    if (StringUtils.hasText(issuerUri)) {
      return JwtDecoders.fromIssuerLocation(issuerUri);
    }
    if (StringUtils.hasText(jwkSetUri)) {
      return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
    if (StringUtils.hasText(hmacSecret)) {
      if (!allowHmacDecoder) {
        throw new IllegalStateException(
            "HMAC JWT decoding is disabled for this profile. Configure FERKO_OIDC_ISSUER_URI "
                + "or FERKO_OIDC_JWK_SET_URI.");
      }
      SecretKey key = new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }
    throw new IllegalStateException(
        "JWT decoder is not configured. Set FERKO_OIDC_ISSUER_URI, FERKO_OIDC_JWK_SET_URI, "
            + "or FERKO_JWT_HMAC_SECRET.");
  }

  private static Collection<GrantedAuthority> extractRoleAuthorities(
      Jwt jwt, String rolesClaimName) {
    Object rolesClaim = jwt.getClaims().get(rolesClaimName);
    if (rolesClaim == null) {
      return List.of();
    }
    if (rolesClaim instanceof Collection<?> roles) {
      return roles.stream()
          .filter(Objects::nonNull)
          .map(Object::toString)
          .filter(StringUtils::hasText)
          .map(WebSecurityConfig::normalizeRole)
          .map(SimpleGrantedAuthority::new)
          .map(GrantedAuthority.class::cast)
          .toList();
    }
    if (rolesClaim instanceof String rolesAsString && StringUtils.hasText(rolesAsString)) {
      return StringUtils.commaDelimitedListToSet(rolesAsString.replace(' ', ',')).stream()
          .filter(StringUtils::hasText)
          .map(WebSecurityConfig::normalizeRole)
          .map(SimpleGrantedAuthority::new)
          .map(GrantedAuthority.class::cast)
          .toList();
    }
    return List.of();
  }

  private static String normalizeRole(String rawRole) {
    String normalized = rawRole.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    if (normalized.startsWith("ROLE_")) {
      return normalized;
    }
    return "ROLE_" + normalized;
  }
}
