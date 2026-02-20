package hr.fer.zemris.ferko.application.usecase.todo;

public class ToDoTaskNotFoundException extends RuntimeException {

  public ToDoTaskNotFoundException(long taskId) {
    super("Task " + taskId + " was not found.");
  }
}
