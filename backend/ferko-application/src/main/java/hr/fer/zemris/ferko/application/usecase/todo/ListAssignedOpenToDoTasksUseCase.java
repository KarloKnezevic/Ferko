package hr.fer.zemris.ferko.application.usecase.todo;

import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.domain.model.ToDoTask;
import hr.fer.zemris.ferko.domain.model.ToDoTaskStatus;
import java.util.Comparator;
import java.util.List;

public class ListAssignedOpenToDoTasksUseCase {

  private final ToDoTaskRepository repository;

  public ListAssignedOpenToDoTasksUseCase(ToDoTaskRepository repository) {
    this.repository = repository;
  }

  public List<ToDoTaskView> execute(long ownerId) {
    return repository.findByOwnerId(ownerId).stream()
        .filter(task -> task.assigneeId() != ownerId)
        .filter(task -> task.status() == ToDoTaskStatus.OPEN)
        .sorted(Comparator.comparing(ToDoTask::deadline).thenComparing(ToDoTask::id))
        .map(ToDoTaskViewMapper::toView)
        .toList();
  }
}
