package hr.fer.zemris.ferko.application.usecase.exchange;

import java.time.LocalDateTime;

/** Read model for a group-exchange request shown in the "burza grupa". */
public record GroupExchangeView(
    long id,
    long courseId,
    String studentJmbag,
    String studentName,
    String fromGroup,
    String toGroup,
    String status,
    String reason,
    String decidedBy,
    LocalDateTime createdAt) {}
