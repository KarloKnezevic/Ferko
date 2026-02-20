package hr.fer.zemris.ferko.application.usecase.todo;

import hr.fer.zemris.ferko.domain.model.ToDoTask;

final class ToDoTaskViewMapper {

  private ToDoTaskViewMapper() {}

  static ToDoTaskView toView(ToDoTask task) {
    return new ToDoTaskView(
        task.id(),
        task.ownerId(),
        task.assigneeId(),
        task.title(),
        task.description(),
        task.deadline(),
        task.priority().name(),
        task.status().name());
  }
}
