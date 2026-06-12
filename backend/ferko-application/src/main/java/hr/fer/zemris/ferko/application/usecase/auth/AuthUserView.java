package hr.fer.zemris.ferko.application.usecase.auth;

import java.util.Set;

/**
 * Application-layer projection of an account for authentication purposes. Roles are exposed as
 * plain strings so the interface layer never depends on domain types.
 */
public record AuthUserView(
    long id,
    String username,
    String passwordHash,
    String fullName,
    String email,
    boolean active,
    Set<String> roles) {}
