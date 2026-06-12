package hr.fer.zemris.ferko.webapi.bootstrap;

import hr.fer.zemris.ferko.application.usecase.auth.ProvisionUserUseCase;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the baseline FERKO accounts (one per role) so the application is usable immediately after a
 * clean start. Disabled in hardened (staging/prod) profiles.
 */
@Component
@Order(5)
@ConditionalOnProperty(
    name = "ferko.seed.users.enabled",
    havingValue = "true",
    matchIfMissing = true)
public class AcademicUserSeeder implements ApplicationRunner {

  private static final String DEFAULT_PASSWORD = "ferko123";

  private record DemoUser(String username, String fullName, String email, Set<String> roles) {}

  private static final List<DemoUser> DEMO_USERS =
      List.of(
          new DemoUser("admin.ferko", "Administrator Ferko", "admin@fer.hr", Set.of("ADMIN")),
          new DemoUser("stuslu.sara", "Sara Studentska", "stuslu@fer.hr", Set.of("STUSLU")),
          new DemoUser(
              "lecturer.marko", "Marko Predavač", "marko@fer.hr", Set.of("NASTAVNIK", "NOSITELJ")),
          new DemoUser("assistant.iva", "Iva Asistent", "iva@fer.hr", Set.of("ASISTENT")),
          new DemoUser("student.ana", "Ana Studentica", "ana@fer.hr", Set.of("STUDENT")));

  private final ProvisionUserUseCase provisionUser;
  private final PasswordEncoder passwordEncoder;

  public AcademicUserSeeder(ProvisionUserUseCase provisionUser, PasswordEncoder passwordEncoder) {
    this.provisionUser = provisionUser;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    String passwordHash = passwordEncoder.encode(DEFAULT_PASSWORD);
    LocalDateTime now = LocalDateTime.now();
    for (DemoUser user : DEMO_USERS) {
      provisionUser.provisionIfAbsent(
          user.username(), passwordHash, user.fullName(), user.email(), user.roles(), now);
    }
  }
}
