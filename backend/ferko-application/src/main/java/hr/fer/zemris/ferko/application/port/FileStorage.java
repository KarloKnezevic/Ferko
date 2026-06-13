package hr.fer.zemris.ferko.application.port;

/** Binary blob storage for uploaded files (local FS or S3-compatible). */
public interface FileStorage {

  /** Stores the content and returns an opaque storage key. */
  String store(byte[] content);

  /** Loads the content for a previously returned key. */
  byte[] load(String storageKey);
}
