package hr.fer.zemris.ferko.webapi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

class WebSecurityConfigTest {

  private static final String TEST_HMAC_SECRET = "ferko-test-hmac-secret-0123456789abcdef";

  private final WebSecurityConfig config = new WebSecurityConfig();

  @Test
  void jwtDecoderAllowsHmacSecretWhenEnabled() {
    assertNotNull(config.jwtDecoder("", "", TEST_HMAC_SECRET, true));
  }

  @Test
  void jwtDecoderRejectsHmacSecretWhenDisabled() {
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> config.jwtDecoder("", "", TEST_HMAC_SECRET, false));

    assertTrue(exception.getMessage().contains("HMAC JWT decoding is disabled"));
  }

  @Test
  void jwtDecoderRejectsMissingConfiguration() {
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> config.jwtDecoder("", "", "", true));

    assertTrue(exception.getMessage().contains("JWT decoder is not configured"));
  }

  @Test
  void jwtDecoderDecodesRs256TokenFromJwkSetUri() throws Exception {
    RSAKey signingKey = new RSAKeyGenerator(2048).keyID("ferko-test-key").generate();
    String jwkSetBody = new JWKSet(signingKey.toPublicJWK()).toString();
    try (JwkSetHttpServer jwkSetServer = JwkSetHttpServer.start(jwkSetBody)) {
      JwtDecoder decoder = config.jwtDecoder("", jwkSetServer.jwkSetUri(), TEST_HMAC_SECRET, false);
      String token = rsaToken(signingKey, "42");
      Jwt jwt = decoder.decode(token);

      assertEquals("42", jwt.getSubject());
    }
  }

  private static String rsaToken(RSAKey rsaKey, String subject) throws JOSEException {
    Instant now = Instant.now();
    JWTClaimsSet claims =
        new JWTClaimsSet.Builder()
            .subject(subject)
            .issuer("ferko-web-api-security-test")
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(300)))
            .claim("scope", "todo.read todo.write")
            .build();
    SignedJWT jwt =
        new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(), claims);
    JWSSigner signer = new RSASSASigner(rsaKey.toPrivateKey());
    jwt.sign(signer);
    return jwt.serialize();
  }

  private static final class JwkSetHttpServer implements AutoCloseable {
    private static final String HOST = "127.0.0.1";

    private final HttpServer delegate;

    private JwkSetHttpServer(HttpServer delegate) {
      this.delegate = delegate;
    }

    static JwkSetHttpServer start(String body) throws IOException {
      HttpServer server = HttpServer.create(new InetSocketAddress(HOST, 0), 0);
      byte[] responseBody = body.getBytes(StandardCharsets.UTF_8);
      server.createContext(
          "/jwks",
          exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, responseBody.length);
            try (var output = exchange.getResponseBody()) {
              output.write(responseBody);
            }
          });
      server.start();
      return new JwkSetHttpServer(server);
    }

    String jwkSetUri() {
      return "http://" + HOST + ":" + delegate.getAddress().getPort() + "/jwks";
    }

    @Override
    public void close() {
      delegate.stop(0);
    }
  }
}
