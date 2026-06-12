package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.AppUser;
import java.util.List;
import java.util.Optional;

/** Persistence port for FERKO accounts and their roles. */
public interface AppUserRepository {

  AppUser save(AppUser user);

  Optional<AppUser> findById(long id);

  Optional<AppUser> findByUsername(String username);

  List<AppUser> findAll();
}
