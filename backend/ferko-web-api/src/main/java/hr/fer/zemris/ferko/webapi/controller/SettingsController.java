package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.webapi.config.FerkoProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the effective FERKO configuration read-only to administrators, so the single typed source
 * ({@link FerkoProperties}) is visible and auditable from the UI. Secrets (JWT HMAC secret) are
 * never returned — only whether they are configured.
 */
@RestController
@RequestMapping("/api/v1/academic")
public class SettingsController {

  private final FerkoProperties properties;

  public SettingsController(FerkoProperties properties) {
    this.properties = properties;
  }

  @GetMapping("/settings")
  @PreAuthorize("hasRole('ADMIN')")
  public SettingsView settings() {
    FerkoProperties.Security security = properties.getSecurity();
    FerkoProperties.Security.Jwt jwt = security.getJwt();
    FerkoProperties.Seed.Academic academic = properties.getSeed().getAcademic();
    FerkoProperties.Grading grading = properties.getGrading();
    FerkoProperties.Scheduler scheduler = properties.getScheduler();
    return new SettingsView(
        new SeedView(
            properties.getSeed().getUsers().isEnabled(),
            academic.isEnabled(),
            academic.getMaxCourses(),
            academic.getMaxStudents()),
        new GradingView(
            grading.getExcellent(),
            grading.getVeryGood(),
            grading.getGood(),
            grading.getSufficient()),
        new SchedulerView(
            scheduler.getDefaultPopulationSize(),
            scheduler.getDefaultIterations(),
            scheduler.getDefaultSeed()),
        new MailView(properties.getMail().isEnabled(), properties.getMail().getFrom()),
        new SecurityView(
            security.getDevToken().isEnabled(),
            security.getLoginRateLimit().isEnabled(),
            security.getLoginRateLimit().getMaxAttempts(),
            security.getLoginRateLimit().getWindowSeconds(),
            !jwt.getIssuerUri().isBlank(),
            !jwt.getHmacSecret().isBlank(),
            jwt.isAllowHmacDecoder()));
  }

  /** Sanitized snapshot of the effective configuration. Contains no secret values. */
  public record SettingsView(
      SeedView seed,
      GradingView grading,
      SchedulerView scheduler,
      MailView mail,
      SecurityView security) {}

  /** Data-seeding configuration. */
  public record SeedView(
      boolean usersEnabled, boolean academicEnabled, int maxCourses, int maxStudents) {}

  /** Default grade thresholds. */
  public record GradingView(int excellent, int veryGood, int good, int sufficient) {}

  /** Default scheduling-engine parameters. */
  public record SchedulerView(int defaultPopulationSize, int defaultIterations, long defaultSeed) {}

  /** Mail dispatch configuration. */
  public record MailView(boolean enabled, String from) {}

  /** Security toggles. Secrets are reported only as configured/not-configured booleans. */
  public record SecurityView(
      boolean devTokenEnabled,
      boolean loginRateLimitEnabled,
      int loginRateLimitMaxAttempts,
      long loginRateLimitWindowSeconds,
      boolean oidcIssuerConfigured,
      boolean jwtHmacSecretConfigured,
      boolean allowHmacDecoder) {}
}
