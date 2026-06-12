package hr.fer.zemris.ferko.application.usecase.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class AuthUseCasesTest {

  @Test
  void provisionsUserOnlyWhenAbsent() {
    InMemoryAppUserRepository repository = new InMemoryAppUserRepository();
    ProvisionUserUseCase provision = new ProvisionUserUseCase(repository);

    boolean created =
        provision.provisionIfAbsent(
            "admin.ferko", "hash", "Admin", "admin@fer.hr", Set.of("ADMIN"), LocalDateTime.now());
    assertTrue(created);
    assertEquals(1, repository.findAll().size());

    boolean createdAgain =
        provision.provisionIfAbsent(
            "admin.ferko", "hash", "Admin", "admin@fer.hr", Set.of("ADMIN"), LocalDateTime.now());
    assertFalse(createdAgain);
    assertEquals(1, repository.findAll().size());
  }

  @Test
  void loadsAuthUserViewWithRoleNames() {
    InMemoryAppUserRepository repository = new InMemoryAppUserRepository();
    ProvisionUserUseCase provision = new ProvisionUserUseCase(repository);
    provision.provisionIfAbsent(
        "lecturer.marko",
        "hash",
        "Marko",
        "marko@fer.hr",
        Set.of("NASTAVNIK", "NOSITELJ"),
        LocalDateTime.now());

    LoadAuthUserUseCase load = new LoadAuthUserUseCase(repository);
    AuthUserView view = load.findByUsername("lecturer.marko").orElseThrow();

    assertEquals("lecturer.marko", view.username());
    assertEquals("hash", view.passwordHash());
    assertTrue(view.active());
    assertEquals(Set.of("NASTAVNIK", "NOSITELJ"), view.roles());
    assertTrue(load.findByUsername("nepostojeci").isEmpty());
  }

  /** Minimal in-memory fake of the user repository for use-case tests. */
  private static final class InMemoryAppUserRepository implements AppUserRepository {
    private final List<AppUser> users = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1000);

    @Override
    public AppUser save(AppUser user) {
      AppUser stored =
          user.id() <= 0
              ? new AppUser(
                  sequence.incrementAndGet(),
                  user.username(),
                  user.passwordHash(),
                  user.fullName(),
                  user.email(),
                  user.active(),
                  user.createdAt(),
                  user.roles())
              : user;
      users.removeIf(existing -> existing.id() == stored.id());
      users.add(stored);
      return stored;
    }

    @Override
    public Optional<AppUser> findById(long id) {
      return users.stream().filter(user -> user.id() == id).findFirst();
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
      return users.stream().filter(user -> user.username().equals(username)).findFirst();
    }

    @Override
    public List<AppUser> findAll() {
      return List.copyOf(users);
    }
  }
}
