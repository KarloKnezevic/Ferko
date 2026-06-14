package hr.fer.zemris.ferko.application.usecase.repository;

import hr.fer.zemris.ferko.application.port.FileStorage;
import hr.fer.zemris.ferko.application.port.RepositoryFileRepository;
import hr.fer.zemris.ferko.domain.model.RepositoryFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Uploads, lists and serves files from a course repository. */
public class RepositoryService {

  private final RepositoryFileRepository fileRepository;
  private final FileStorage fileStorage;

  public RepositoryService(RepositoryFileRepository fileRepository, FileStorage fileStorage) {
    this.fileRepository = fileRepository;
    this.fileStorage = fileStorage;
  }

  public long upload(
      long courseId, String filename, String contentType, byte[] content, String uploadedBy) {
    if (filename == null || filename.isBlank()) {
      throw new IllegalArgumentException("Datoteka mora imati naziv.");
    }
    if (content == null || content.length == 0) {
      throw new IllegalArgumentException("Datoteka je prazna.");
    }
    String key = fileStorage.store(content);
    RepositoryFile saved =
        fileRepository.save(
            new RepositoryFile(
                0L,
                courseId,
                filename,
                contentType == null ? "application/octet-stream" : contentType,
                content.length,
                key,
                uploadedBy,
                LocalDateTime.now()));
    return saved.id();
  }

  public List<RepositoryViews.FileView> list(long courseId) {
    return fileRepository.findByCourse(courseId).stream().map(RepositoryService::toView).toList();
  }

  /** Returns the course a file belongs to, for row-level access checks. */
  public Optional<Long> courseIdForFile(long fileId) {
    return fileRepository.findById(fileId).map(RepositoryFile::courseId);
  }

  public Optional<RepositoryViews.DownloadedFile> download(long fileId) {
    return fileRepository
        .findById(fileId)
        .map(
            file ->
                new RepositoryViews.DownloadedFile(
                    file.filename(), file.contentType(), fileStorage.load(file.storageKey())));
  }

  private static RepositoryViews.FileView toView(RepositoryFile file) {
    return new RepositoryViews.FileView(
        file.id(),
        file.courseId(),
        file.filename(),
        file.contentType(),
        file.sizeBytes(),
        file.uploadedBy(),
        file.uploadedAt());
  }
}
