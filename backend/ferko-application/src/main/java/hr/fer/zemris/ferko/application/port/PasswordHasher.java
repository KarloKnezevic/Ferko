package hr.fer.zemris.ferko.application.port;

/**
 * Hashes a raw password for storage. Keeps the concrete encoder (BCrypt, etc.) outside the
 * application layer: the adapter wires it to the framework's {@code PasswordEncoder}.
 */
public interface PasswordHasher {

  /** Returns a secure one-way hash of {@code rawPassword}, suitable for persisting. */
  String hash(String rawPassword);
}
