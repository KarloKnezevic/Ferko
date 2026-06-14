package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

/** An entry in a user's e-portfolio (project, achievement or skill). */
public record PortfolioEntry(
    long id,
    long userId,
    String title,
    String description,
    String category,
    String link,
    LocalDateTime createdAt) {}
