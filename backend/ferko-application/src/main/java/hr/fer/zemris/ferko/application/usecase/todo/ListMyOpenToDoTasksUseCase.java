package hr.fer.zemris.ferko.application.usecase.todo;

import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.domain.model.ToDoTask;
import hr.fer.zemris.ferko.domain.model.ToDoTaskStatus;
import java.util.Comparator;
import java.util.List;

public class ListMyOpenToDoTasksUseCase {

  private final ToDoTaskRepository repository;

  public ListMyOpenToDoTasksUseCase(ToDoTaskRepository repository) {
    this.repository = repository;
  }

  public List<ToDoTaskView> execute(long assigneeId) {
    return repository.findByAssigneeId(assigneeId).stream()
        .filter(task -> task.status() == ToDoTaskStatus.OPEN)
        .sorted(Comparator.comparing(ToDoTask::deadline).thenComparing(ToDoTask::id))
        .map(ToDoTaskViewMapper::toView)
        .toList();
  }
}
