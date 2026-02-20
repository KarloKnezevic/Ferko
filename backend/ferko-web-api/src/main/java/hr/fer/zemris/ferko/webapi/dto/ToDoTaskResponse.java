package hr.fer.zemris.ferko.webapi.dto;

import java.time.LocalDateTime;

public record ToDoTaskResponse(
    long id,
    long ownerId,
    long assigneeId,
    String title,
    String description,
    LocalDateTime deadline,
    String priority,
    String status) {}
