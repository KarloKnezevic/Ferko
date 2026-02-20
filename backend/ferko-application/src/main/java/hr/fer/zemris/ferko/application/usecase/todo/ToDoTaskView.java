package hr.fer.zemris.ferko.application.usecase.todo;

import java.time.LocalDateTime;

public record ToDoTaskView(
    long id,
    long ownerId,
    long assigneeId,
    String title,
    String description,
    LocalDateTime deadline,
    String priority,
    String status) {}
