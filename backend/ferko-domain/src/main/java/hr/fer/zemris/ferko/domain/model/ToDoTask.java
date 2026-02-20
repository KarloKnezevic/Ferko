package hr.fer.zemris.ferko.domain.model;

import java.time.LocalDateTime;

public record ToDoTask(
    long id,
    long ownerId,
    long assigneeId,
    String title,
    String description,
    LocalDateTime deadline,
    ToDoTaskPriority priority,
    ToDoTaskStatus status) {

  public ToDoTask {
    if (id <= 0L) {
      throw new IllegalArgumentException("Task id must be positive.");
    }
    if (ownerId <= 0L) {
      throw new IllegalArgumentException("Owner id must be positive.");
    }
    if (assigneeId <= 0L) {
      throw new IllegalArgumentException("Assignee id must be positive.");
    }
    if (title == null || title.isBlank()) {
      throw new IllegalArgumentException("Title must not be blank.");
    }
    if (deadline == null) {
      throw new IllegalArgumentException("Deadline must not be null.");
    }
    if (priority == null) {
      throw new IllegalArgumentException("Priority must not be null.");
    }
    if (status == null) {
      throw new IllegalArgumentException("Status must not be null.");
    }

    title = title.trim();
    description = description == null ? "" : description.trim();
  }

  public static ToDoTask open(
      long id,
      long ownerId,
      long assigneeId,
      String title,
      String description,
      LocalDateTime deadline,
      ToDoTaskPriority priority) {
    return new ToDoTask(
        id, ownerId, assigneeId, title, description, deadline, priority, ToDoTaskStatus.OPEN);
  }

  public boolean isManagedBy(long userId) {
    return userId > 0L && (ownerId == userId || assigneeId == userId);
  }

  public ToDoTask close() {
    if (status == ToDoTaskStatus.CLOSED) {
      return this;
    }
    return new ToDoTask(
        id, ownerId, assigneeId, title, description, deadline, priority, ToDoTaskStatus.CLOSED);
  }
}
