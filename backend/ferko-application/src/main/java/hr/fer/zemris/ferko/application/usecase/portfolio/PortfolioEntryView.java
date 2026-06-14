package hr.fer.zemris.ferko.application.usecase.portfolio;

import java.time.LocalDateTime;

/** Read model for an e-portfolio entry. */
public record PortfolioEntryView(
    long id,
    String title,
    String description,
    String category,
    String link,
    LocalDateTime createdAt) {}
