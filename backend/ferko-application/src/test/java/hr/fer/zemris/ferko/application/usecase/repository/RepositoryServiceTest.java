package hr.fer.zemris.ferko.application.usecase.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hr.fer.zemris.ferko.application.port.FileStorage;
import hr.fer.zemris.ferko.application.port.RepositoryFileRepository;
import hr.fer.zemris.ferko.domain.model.RepositoryFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RepositoryServiceTest {

  private static final class FakeFiles implements RepositoryFileRepository {
    private final List<RepositoryFile> store = new ArrayList<>();
    private long seq = 0;

    @Override
    public RepositoryFile save(RepositoryFile f) {
      RepositoryFile saved =
          new RepositoryFile(
              ++seq,
              f.courseId(),
              f.filename(),
              f.contentType(),
              f.sizeBytes(),
              f.storageKey(),
              f.uploadedBy(),
              f.uploadedAt());
      store.add(saved);
      return saved;
    }

    @Override
    public List<RepositoryFile> findByCourse(long courseId) {
      return store.stream().filter(f -> f.courseId() == courseId).toList();
    }

    @Override
    public Optional<RepositoryFile> findById(long id) {
      return store.stream().filter(f -> f.id() == id).findFirst();
    }
  }

  private static final class FakeStorage implements FileStorage {
    private final Map<String, byte[]> blobs = new HashMap<>();
    private long seq = 0;

    @Override
    public String store(byte[] content) {
      String key = "k" + (++seq);
      blobs.put(key, content);
      return key;
    }

    @Override
    public byte[] load(String storageKey) {
      return blobs.get(storageKey);
    }
  }

  @Test
  void uploadsListsAndDownloads() {
    RepositoryService service = new RepositoryService(new FakeFiles(), new FakeStorage());
    byte[] content = "sadrzaj".getBytes(StandardCharsets.UTF_8);

    long id = service.upload(5L, "skripta.pdf", "application/pdf", content, "lecturer.marko");
    assertTrue(id > 0);

    List<RepositoryViews.FileView> files = service.list(5L);
    assertEquals(1, files.size());
    assertEquals("skripta.pdf", files.get(0).filename());
    assertEquals(content.length, files.get(0).sizeBytes());

    RepositoryViews.DownloadedFile downloaded = service.download(id).orElseThrow();
    assertArrayEquals(content, downloaded.content());
    assertEquals("application/pdf", downloaded.contentType());
  }

  @Test
  void rejectsBlankNameOrEmptyContent() {
    RepositoryService service = new RepositoryService(new FakeFiles(), new FakeStorage());
    assertThrows(
        IllegalArgumentException.class,
        () -> service.upload(5L, " ", "text/plain", new byte[] {1}, "u"));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.upload(5L, "a.txt", "text/plain", new byte[0], "u"));
  }
}
