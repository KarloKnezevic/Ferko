package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/** Metadata for a file in a course repository; binary content lives in the file storage. */
public record RepositoryFile(
    long id,
    long courseId,
    String filename,
    String contentType,
    long sizeBytes,
    String storageKey,
    String uploadedBy,
    LocalDateTime uploadedAt) {}
