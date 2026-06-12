package hr.fer.zemris.ferko.webapi.auth;

import hr.fer.zemris.ferko.application.usecase.auth.AuthUserView;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Authenticated FERKO principal carrying identity and display data alongside authorities. */
public final class FerkoPrincipal implements UserDetails {

  private final long id;
  private final String username;
  private final String passwordHash;
  private final String fullName;
  private final boolean active;
  private final List<GrantedAuthority> authorities;

  public FerkoPrincipal(AuthUserView user) {
    this.id = user.id();
    this.username = user.username();
    this.passwordHash = user.passwordHash();
    this.fullName = user.fullName();
    this.active = user.active();
    this.authorities =
        user.roles().stream()
            .map(role -> "ROLE_" + role)
            .map(SimpleGrantedAuthority::new)
            .map(GrantedAuthority.class::cast)
            .toList();
  }

  public long id() {
    return id;
  }

  public String fullName() {
    return fullName;
  }

  public List<String> roleNames() {
    return authorities.stream().map(GrantedAuthority::getAuthority).toList();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return active;
  }
}
