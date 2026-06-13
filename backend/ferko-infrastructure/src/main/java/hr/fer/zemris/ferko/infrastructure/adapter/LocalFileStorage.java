package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.FileStorage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Stores blobs as files under a base directory; the storage key is the file name. */
public class LocalFileStorage implements FileStorage {

  private final Path baseDir;

  public LocalFileStorage(String baseDir) {
    this.baseDir = Path.of(baseDir);
    try {
      Files.createDirectories(this.baseDir);
    } catch (IOException ex) {
      throw new UncheckedIOException("Ne mogu stvoriti direktorij za pohranu: " + baseDir, ex);
    }
  }

  @Override
  public String store(byte[] content) {
    String key = UUID.randomUUID().toString().replace("-", "");
    try {
      Files.write(baseDir.resolve(key), content);
    } catch (IOException ex) {
      throw new UncheckedIOException("Pohrana datoteke nije uspjela.", ex);
    }
    return key;
  }

  @Override
  public byte[] load(String storageKey) {
    try {
      return Files.readAllBytes(baseDir.resolve(storageKey));
    } catch (IOException ex) {
      throw new UncheckedIOException("Čitanje datoteke nije uspjelo: " + storageKey, ex);
    }
  }
}
