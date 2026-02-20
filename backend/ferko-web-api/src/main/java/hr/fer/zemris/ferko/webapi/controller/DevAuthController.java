package hr.fer.zemris.ferko.webapi.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.swagger.v3.oas.annotations.Hidden;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/dev")
@Hidden
@Profile("!staging & !prod")
@ConditionalOnProperty(name = "ferko.security.dev-token.enabled", havingValue = "true")
public class DevAuthController {

  private static final String DEFAULT_SCOPE = "todo.read todo.write";
  private static final List<String> DEFAULT_ROLES = List.of("TODO_READ", "TODO_WRITE");
  private static final int DEFAULT_EXPIRES_IN_SECONDS = 3600;
  private static final int MIN_EXPIRES_IN_SECONDS = 60;
  private static final int MAX_EXPIRES_IN_SECONDS = 86400;

  private final String hmacSecret;
  private final String principalClaim;
  private final String rolesClaimName;

  public DevAuthController(
      @Value("${ferko.security.jwt.hmac-secret:}") String hmacSecret,
      @Value("${ferko.security.jwt.principal-claim:sub}") String principalClaim,
      @Value("${ferko.security.jwt.roles-claim:roles}") String rolesClaimName) {
    this.hmacSecret = hmacSecret;
    this.principalClaim = principalClaim;
    this.rolesClaimName = rolesClaimName;
  }

  @PostMapping("/token")
  public DevTokenResponse issueToken(@RequestBody DevTokenRequest request) {
    long userId = requireUserId(request.userId());
    String scope = normalizeScope(request.scope());
    long expiresInSeconds = normalizeExpiresInSeconds(request.expiresInSeconds());
    List<String> roles = normalizeRoles(request.roles());

    String token = signToken(userId, scope, roles, expiresInSeconds);
    return new DevTokenResponse(
        "Bearer", token, "Bearer " + token, expiresInSeconds, userId, scope, roles);
  }

  private String signToken(long userId, String scope, List<String> roles, long expiresInSeconds) {
    if (!StringUtils.hasText(hmacSecret)) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "Development token issuing requires FERKO_JWT_HMAC_SECRET.");
    }

    byte[] keyBytes = hmacSecret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "FERKO_JWT_HMAC_SECRET must contain at least 32 bytes for HS256 signing.");
    }

    Instant now = Instant.now();
    JWTClaimsSet.Builder claimsBuilder =
        new JWTClaimsSet.Builder()
            .subject(String.valueOf(userId))
            .issuer("ferko-dev-token")
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(expiresInSeconds)))
            .claim("scope", scope)
            .claim("roles", roles);

    if (!"sub".equals(principalClaim)) {
      claimsBuilder.claim(principalClaim, String.valueOf(userId));
    }
    if (!"roles".equals(rolesClaimName)) {
      claimsBuilder.claim(rolesClaimName, roles);
    }

    SignedJWT signedJwt =
        new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).build(), claimsBuilder.build());
    try {
      JWSSigner signer = new MACSigner(keyBytes);
      signedJwt.sign(signer);
    } catch (JOSEException ex) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create development token.", ex);
    }
    return signedJwt.serialize();
  }

  private static long requireUserId(Long userId) {
    if (userId == null || userId <= 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "userId must be a positive numeric identifier.");
    }
    return userId;
  }

  private static String normalizeScope(String scope) {
    if (scope == null || scope.isBlank()) {
      return DEFAULT_SCOPE;
    }
    return scope.trim().replaceAll("\\s+", " ");
  }

  private static List<String> normalizeRoles(List<String> roles) {
    if (roles == null || roles.isEmpty()) {
      return DEFAULT_ROLES;
    }
    return roles.stream().filter(StringUtils::hasText).map(String::trim).distinct().toList();
  }

  private static int normalizeExpiresInSeconds(Integer expiresInSeconds) {
    if (expiresInSeconds == null) {
      return DEFAULT_EXPIRES_IN_SECONDS;
    }
    if (expiresInSeconds < MIN_EXPIRES_IN_SECONDS || expiresInSeconds > MAX_EXPIRES_IN_SECONDS) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "expiresInSeconds must be between "
              + MIN_EXPIRES_IN_SECONDS
              + " and "
              + MAX_EXPIRES_IN_SECONDS
              + ".");
    }
    return expiresInSeconds;
  }

  public record DevTokenRequest(
      Long userId, String scope, List<String> roles, Integer expiresInSeconds) {}

  public record DevTokenResponse(
      String tokenType,
      String token,
      String authorizationHeader,
      long expiresInSeconds,
      long userId,
      String scope,
      List<String> roles) {}
}
