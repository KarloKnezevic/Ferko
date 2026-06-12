package hr.fer.zemris.ferko.webapi.auth;

import hr.fer.zemris.ferko.application.usecase.auth.LoadAuthUserUseCase;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/** Loads FERKO accounts for Spring Security form/session authentication. */
public class FerkoUserDetailsService implements UserDetailsService {

  private final LoadAuthUserUseCase loadAuthUser;

  public FerkoUserDetailsService(LoadAuthUserUseCase loadAuthUser) {
    this.loadAuthUser = loadAuthUser;
  }

  @Override
  public UserDetails loadUserByUsername(String username) {
    return loadAuthUser
        .findByUsername(username)
        .map(FerkoPrincipal::new)
        .orElseThrow(() -> new UsernameNotFoundException("Nepoznat korisnik: " + username));
  }
}
