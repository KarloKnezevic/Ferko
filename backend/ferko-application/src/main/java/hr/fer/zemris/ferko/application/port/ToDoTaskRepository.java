package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.ToDoTask;
import java.util.List;
import java.util.Optional;

public interface ToDoTaskRepository {

  long nextIdentity();

  void save(ToDoTask task);

  Optional<ToDoTask> findById(long taskId);

  List<ToDoTask> findByAssigneeId(long assigneeId);

  List<ToDoTask> findByOwnerId(long ownerId);
}
