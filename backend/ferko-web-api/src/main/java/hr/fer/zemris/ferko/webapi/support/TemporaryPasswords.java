package hr.fer.zemris.ferko.webapi.support;

import java.security.SecureRandom;

/**
 * Generates one-time passwords for admin-initiated resets. Lives at the web boundary so a
 * cryptographic RNG ({@link SecureRandom}) is used without leaking a {@code java.security}
 * dependency into the application layer.
 */
public final class TemporaryPasswords {

  // Unambiguous alphabet (no 0/O/1/l/I) so the password is easy to read out loud once.
  private static final char[] ALPHABET =
      "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789".toCharArray();
  private static final int LENGTH = 12;
  private static final SecureRandom RANDOM = new SecureRandom();

  private TemporaryPasswords() {}

  /** Returns a fresh 12-character one-time password drawn from an unambiguous alphabet. */
  public static String generate() {
    StringBuilder builder = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      builder.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
    }
    return builder.toString();
  }
}
