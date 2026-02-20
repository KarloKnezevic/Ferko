package hr.fer.zemris.ferko.application.usecase.todo;

import java.time.LocalDateTime;

public record CreateToDoTaskCommand(
    long ownerId,
    long assigneeId,
    String title,
    String description,
    LocalDateTime deadline,
    String priority) {}
