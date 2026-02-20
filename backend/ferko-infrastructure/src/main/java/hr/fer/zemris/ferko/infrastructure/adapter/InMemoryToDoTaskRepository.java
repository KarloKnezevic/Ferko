package hr.fer.zemris.ferko.infrastructure.adapter;

import hr.fer.zemris.ferko.application.port.ToDoTaskRepository;
import hr.fer.zemris.ferko.domain.model.ToDoTask;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryToDoTaskRepository implements ToDoTaskRepository {

  private final AtomicLong sequence = new AtomicLong(0L);
  private final ConcurrentMap<Long, ToDoTask> tasks = new ConcurrentHashMap<>();

  @Override
  public long nextIdentity() {
    return sequence.incrementAndGet();
  }

  @Override
  public void save(ToDoTask task) {
    tasks.put(task.id(), task);
  }

  @Override
  public Optional<ToDoTask> findById(long taskId) {
    return Optional.ofNullable(tasks.get(taskId));
  }

  @Override
  public List<ToDoTask> findByAssigneeId(long assigneeId) {
    List<ToDoTask> result = new ArrayList<>();
    for (ToDoTask task : tasks.values()) {
      if (task.assigneeId() == assigneeId) {
        result.add(task);
      }
    }
    return result;
  }

  @Override
  public List<ToDoTask> findByOwnerId(long ownerId) {
    List<ToDoTask> result = new ArrayList<>();
    for (ToDoTask task : tasks.values()) {
      if (task.ownerId() == ownerId) {
        result.add(task);
      }
    }
    return result;
  }
}
