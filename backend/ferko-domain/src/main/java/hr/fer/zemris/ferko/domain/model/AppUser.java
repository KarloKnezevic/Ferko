package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** A FERKO account with its assigned roles. */
public record AppUser(
    long id,
    String username,
    String passwordHash,
    String fullName,
    String email,
    boolean active,
    LocalDateTime createdAt,
    Set<Role> roles) {

  public AppUser {
    roles = roles == null ? Set.of() : Collections.unmodifiableSet(EnumSet.copyOf(roles));
  }

  public boolean hasRole(Role role) {
    return roles.contains(role);
  }
}
