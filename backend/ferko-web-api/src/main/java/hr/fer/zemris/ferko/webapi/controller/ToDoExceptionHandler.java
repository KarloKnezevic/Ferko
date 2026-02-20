package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.todo.ToDoTaskAccessDeniedException;
import hr.fer.zemris.ferko.application.usecase.todo.ToDoTaskNotFoundException;
import hr.fer.zemris.ferko.webapi.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ToDoExceptionHandler {

  @ExceptionHandler(ToDoTaskNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleNotFound(ToDoTaskNotFoundException ex) {
    return new ErrorResponse(ex.getMessage());
  }

  @ExceptionHandler(ToDoTaskAccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ErrorResponse handleForbidden(ToDoTaskAccessDeniedException ex) {
    return new ErrorResponse(ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleBadRequest(IllegalArgumentException ex) {
    return new ErrorResponse(ex.getMessage());
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
    String reason = ex.getReason();
    String message = reason == null || reason.isBlank() ? ex.getStatusCode().toString() : reason;
    return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse(message));
  }
}
