package hr.fer.zemris.ferko.application.usecase.auth;

import hr.fer.zemris.ferko.application.port.AppUserRepository;
import hr.fer.zemris.ferko.domain.model.AppUser;
import hr.fer.zemris.ferko.domain.model.Role;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Loads an account by username for the authentication layer. */
public class LoadAuthUserUseCase {

  private final AppUserRepository userRepository;

  public LoadAuthUserUseCase(AppUserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Optional<AuthUserView> findByUsername(String username) {
    return userRepository.findByUsername(username).map(LoadAuthUserUseCase::toView);
  }

  private static AuthUserView toView(AppUser user) {
    Set<String> roleNames = user.roles().stream().map(Role::name).collect(Collectors.toSet());
    return new AuthUserView(
        user.id(),
        user.username(),
        user.passwordHash(),
        user.fullName(),
        user.email(),
        user.active(),
        roleNames);
  }
}
