package hr.fer.zemris.ferko.application.usecase.repository;

import java.time.LocalDateTime;

/** Read models for the course file repository. */
public final class RepositoryViews {

  private RepositoryViews() {}

  /** File metadata (no binary content). */
  public record FileView(
      long id,
      long courseId,
      String filename,
      String contentType,
      long sizeBytes,
      String uploadedBy,
      LocalDateTime uploadedAt) {}

  /** A file ready to download: metadata + binary content. */
  public record DownloadedFile(String filename, String contentType, byte[] content) {}
}
