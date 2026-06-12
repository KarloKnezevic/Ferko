package hr.fer.zemris.ferko.application.usecase.auth;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Role;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/** Creates accounts that do not yet exist (used for seeding demo/admin users). */
public class ProvisionUserUseCase {

  private final AppUserRepository userRepository;

  public ProvisionUserUseCase(AppUserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Provisions an account if no account with the given username exists.
   *
   * @return {@code true} if a new account was created, {@code false} if it already existed
   */
  public boolean provisionIfAbsent(
      String username,
      String passwordHash,
      String fullName,
      String email,
      Set<String> roleNames,
      LocalDateTime createdAt) {
    if (userRepository.findByUsername(username).isPresent()) {
      return false;
    }
    Set<Role> roles =
        roleNames.stream()
            .map(Role::valueOf)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
    userRepository.save(
        new AppUser(0L, username, passwordHash, fullName, email, true, createdAt, roles));
    return true;
  }
}
