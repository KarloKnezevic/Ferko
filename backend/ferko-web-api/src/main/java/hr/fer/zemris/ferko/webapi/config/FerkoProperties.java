package hr.fer.zemris.ferko.webapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Single typed source of FERKO-specific configuration (prefix {@code ferko}).
 *
 * <p>Historically these knobs were scattered across {@code @Value} injections in several beans.
 * This class consolidates them so that every tunable behaviour — data seeding scale, grade
 * thresholds, scheduler defaults, mail and security toggles — lives in one place that can be
 * documented and surfaced read-only to administrators (see {@code SettingsController}).
 *
 * <p>Values are still overridden through environment variables / {@code application.yml}; this is
 * the typed binding, not a competing source of truth.
 */
@ConfigurationProperties(prefix = "ferko")
public class FerkoProperties {

  @NestedConfigurationProperty private final Mail mail = new Mail();
  @NestedConfigurationProperty private final Security security = new Security();
  @NestedConfigurationProperty private final Seed seed = new Seed();
  @NestedConfigurationProperty private final Grading grading = new Grading();
  @NestedConfigurationProperty private final Scheduler scheduler = new Scheduler();

  public Mail getMail() {
    return mail;
  }

  public Security getSecurity() {
    return security;
  }

  public Seed getSeed() {
    return seed;
  }

  public Grading getGrading() {
    return grading;
  }

  public Scheduler getScheduler() {
    return scheduler;
  }

  /** Outbound e-mail settings. SMTP host/port live under {@code spring.mail.*}. */
  public static class Mail {
    private boolean enabled = false;
    private String from = "ferko@fer.hr";

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getFrom() {
      return from;
    }

    public void setFrom(String from) {
      this.from = from;
    }
  }

  /** Authentication and login-protection settings. */
  public static class Security {
    @NestedConfigurationProperty private final Jwt jwt = new Jwt();
    @NestedConfigurationProperty private final DevToken devToken = new DevToken();
    @NestedConfigurationProperty private final LoginRateLimit loginRateLimit = new LoginRateLimit();

    public Jwt getJwt() {
      return jwt;
    }

    public DevToken getDevToken() {
      return devToken;
    }

    public LoginRateLimit getLoginRateLimit() {
      return loginRateLimit;
    }

    /** JWT / OIDC decoding configuration. */
    public static class Jwt {
      private String principalClaim = "sub";
      private String rolesClaim = "roles";
      private String issuerUri = "";
      private String jwkSetUri = "";
      private String hmacSecret = "";
      private boolean allowHmacDecoder = true;

      public String getPrincipalClaim() {
        return principalClaim;
      }

      public void setPrincipalClaim(String principalClaim) {
        this.principalClaim = principalClaim;
      }

      public String getRolesClaim() {
        return rolesClaim;
      }

      public void setRolesClaim(String rolesClaim) {
        this.rolesClaim = rolesClaim;
      }

      public String getIssuerUri() {
        return issuerUri;
      }

      public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
      }

      public String getJwkSetUri() {
        return jwkSetUri;
      }

      public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
      }

      public String getHmacSecret() {
        return hmacSecret;
      }

      public void setHmacSecret(String hmacSecret) {
        this.hmacSecret = hmacSecret;
      }

      public boolean isAllowHmacDecoder() {
        return allowHmacDecoder;
      }

      public void setAllowHmacDecoder(boolean allowHmacDecoder) {
        this.allowHmacDecoder = allowHmacDecoder;
      }
    }

    /** Local development token endpoint toggle. */
    public static class DevToken {
      private boolean enabled = false;

      public boolean isEnabled() {
        return enabled;
      }

      public void setEnabled(boolean enabled) {
        this.enabled = enabled;
      }
    }

    /** Brute-force protection for the form-login endpoint. */
    public static class LoginRateLimit {
      private boolean enabled = false;
      private int maxAttempts = 10;
      private long windowSeconds = 60;

      public boolean isEnabled() {
        return enabled;
      }

      public void setEnabled(boolean enabled) {
        this.enabled = enabled;
      }

      public int getMaxAttempts() {
        return maxAttempts;
      }

      public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
      }

      public long getWindowSeconds() {
        return windowSeconds;
      }

      public void setWindowSeconds(long windowSeconds) {
        this.windowSeconds = windowSeconds;
      }
    }
  }

  /** Bootstrap/demo data seeding settings. */
  public static class Seed {
    @NestedConfigurationProperty private final Users users = new Users();
    @NestedConfigurationProperty private final Academic academic = new Academic();

    public Users getUsers() {
      return users;
    }

    public Academic getAcademic() {
      return academic;
    }

    /** Demo users seeding. */
    public static class Users {
      private boolean enabled = true;

      public boolean isEnabled() {
        return enabled;
      }

      public void setEnabled(boolean enabled) {
        this.enabled = enabled;
      }
    }

    /**
     * Academic dataset seeding. {@code maxCourses}/{@code maxStudents} of {@code 0} or below mean
     * "no limit" — seed the entire bundled dataset.
     */
    public static class Academic {
      private boolean enabled = true;
      private int maxCourses = 12;
      private int maxStudents = 120;

      public boolean isEnabled() {
        return enabled;
      }

      public void setEnabled(boolean enabled) {
        this.enabled = enabled;
      }

      public int getMaxCourses() {
        return maxCourses;
      }

      public void setMaxCourses(int maxCourses) {
        this.maxCourses = maxCourses;
      }

      public int getMaxStudents() {
        return maxStudents;
      }

      public void setMaxStudents(int maxStudents) {
        this.maxStudents = maxStudents;
      }
    }
  }

  /**
   * Default grade thresholds applied when a course does not define its own. Points at or above a
   * threshold earn the corresponding grade; {@code sufficient} is the minimum passing score and is
   * always required for a passing grade.
   */
  public static class Grading {
    private int excellent = 88;
    private int veryGood = 75;
    private int good = 62;
    private int sufficient = 50;

    public int getExcellent() {
      return excellent;
    }

    public void setExcellent(int excellent) {
      this.excellent = excellent;
    }

    public int getVeryGood() {
      return veryGood;
    }

    public void setVeryGood(int veryGood) {
      this.veryGood = veryGood;
    }

    public int getGood() {
      return good;
    }

    public void setGood(int good) {
      this.good = good;
    }

    public int getSufficient() {
      return sufficient;
    }

    public void setSufficient(int sufficient) {
      this.sufficient = sufficient;
    }
  }

  /** Default metaheuristic parameters used by the scheduling engine. */
  public static class Scheduler {
    private int defaultPopulationSize = 60;
    private int defaultIterations = 5000;
    private long defaultSeed = 42;

    public int getDefaultPopulationSize() {
      return defaultPopulationSize;
    }

    public void setDefaultPopulationSize(int defaultPopulationSize) {
      this.defaultPopulationSize = defaultPopulationSize;
    }

    public int getDefaultIterations() {
      return defaultIterations;
    }

    public void setDefaultIterations(int defaultIterations) {
      this.defaultIterations = defaultIterations;
    }

    public long getDefaultSeed() {
      return defaultSeed;
    }

    public void setDefaultSeed(long defaultSeed) {
      this.defaultSeed = defaultSeed;
    }
  }
}
