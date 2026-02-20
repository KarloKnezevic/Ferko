package hr.fer.zemris.ferko.application.usecase.todo;

public class ToDoTaskAccessDeniedException extends RuntimeException {

  public ToDoTaskAccessDeniedException(long taskId, long actorUserId) {
    super("User " + actorUserId + " cannot manage task " + taskId + ".");
  }
}
