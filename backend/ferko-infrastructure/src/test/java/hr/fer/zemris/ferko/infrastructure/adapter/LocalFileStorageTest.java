package hr.fer.zemris.ferko.infrastructure.adapter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileStorageTest {

  @Test
  void storesAndLoadsBytesRoundTrip(@TempDir Path dir) {
    LocalFileStorage storage = new LocalFileStorage(dir.resolve("files").toString());
    byte[] content = "FERKO skripta".getBytes(StandardCharsets.UTF_8);

    String key = storage.store(content);
    assertNotNull(key);
    assertArrayEquals(content, storage.load(key));

    // Distinct uploads get distinct keys.
    assertNotEquals(key, storage.store(content));
  }
}
