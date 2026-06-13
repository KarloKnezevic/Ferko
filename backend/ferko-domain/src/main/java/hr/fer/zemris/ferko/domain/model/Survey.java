package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/** A course evaluation survey ("anketa"). */
public record Survey(
    long id, long courseId, String title, boolean active, LocalDateTime createdAt) {}
