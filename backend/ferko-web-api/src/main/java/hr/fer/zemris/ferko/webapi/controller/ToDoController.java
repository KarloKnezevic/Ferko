package hr.fer.zemris.ferko.webapi.controller;

import hr.fer.zemris.ferko.application.usecase.todo.CloseToDoTaskUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.CreateToDoTaskCommand;
import hr.fer.zemris.ferko.application.usecase.todo.CreateToDoTaskUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.ListAssignedOpenToDoTasksUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.ListMyOpenToDoTasksUseCase;
import hr.fer.zemris.ferko.application.usecase.todo.ToDoTaskView;
import hr.fer.zemris.ferko.webapi.dto.CreateToDoTaskRequest;
import hr.fer.zemris.ferko.webapi.dto.ErrorResponse;
import hr.fer.zemris.ferko.webapi.dto.ToDoTaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/todo")
@Tag(name = "ToDo", description = "Create, list, and close ToDo tasks.")
@SecurityRequirement(name = "bearerAuth")
public class ToDoController {

  private final CreateToDoTaskUseCase createToDoTaskUseCase;
  private final ListMyOpenToDoTasksUseCase listMyOpenToDoTasksUseCase;
  private final ListAssignedOpenToDoTasksUseCase listAssignedOpenToDoTasksUseCase;
  private final CloseToDoTaskUseCase closeToDoTaskUseCase;

  public ToDoController(
      CreateToDoTaskUseCase createToDoTaskUseCase,
      ListMyOpenToDoTasksUseCase listMyOpenToDoTasksUseCase,
      ListAssignedOpenToDoTasksUseCase listAssignedOpenToDoTasksUseCase,
      CloseToDoTaskUseCase closeToDoTaskUseCase) {
    this.createToDoTaskUseCase = createToDoTaskUseCase;
    this.listMyOpenToDoTasksUseCase = listMyOpenToDoTasksUseCase;
    this.listAssignedOpenToDoTasksUseCase = listAssignedOpenToDoTasksUseCase;
    this.closeToDoTaskUseCase = closeToDoTaskUseCase;
  }

  @PostMapping("/tasks")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(
      summary = "Create ToDo task",
      description =
          "Creates a new task where caller becomes owner and requested assignee is assigned.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Task created.",
        content = @Content(schema = @Schema(implementation = ToDoTaskResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid request payload.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing or invalid bearer token.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks todo.write authority.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ToDoTaskResponse createTask(
      @RequestBody CreateToDoTaskRequest request, Principal principal) {
    long ownerId = requireAuthenticatedUserId(principal);
    ToDoTaskView task =
        createToDoTaskUseCase.execute(
            new CreateToDoTaskCommand(
                ownerId,
                request.assigneeId(),
                request.title(),
                request.description(),
                request.deadline(),
                request.priority()));
    return toResponse(task);
  }

  @GetMapping("/my")
  @Operation(
      summary = "List my open tasks",
      description = "Lists open tasks where caller is assignee.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "List of open tasks.",
        content =
            @Content(
                array = @ArraySchema(schema = @Schema(implementation = ToDoTaskResponse.class)))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing or invalid bearer token.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks todo.read authority.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public List<ToDoTaskResponse> listMyOpenTasks(Principal principal) {
    long userId = requireAuthenticatedUserId(principal);
    return listMyOpenToDoTasksUseCase.execute(userId).stream()
        .map(ToDoController::toResponse)
        .toList();
  }

  @GetMapping("/assigned")
  @Operation(
      summary = "List tasks assigned by me",
      description = "Lists open tasks where caller is owner and task is assigned to others.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "List of open tasks.",
        content =
            @Content(
                array = @ArraySchema(schema = @Schema(implementation = ToDoTaskResponse.class)))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing or invalid bearer token.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks todo.read authority.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public List<ToDoTaskResponse> listAssignedOpenTasks(Principal principal) {
    long userId = requireAuthenticatedUserId(principal);
    return listAssignedOpenToDoTasksUseCase.execute(userId).stream()
        .map(ToDoController::toResponse)
        .toList();
  }

  @PostMapping("/tasks/{taskId}/close")
  @Operation(
      summary = "Close task",
      description = "Closes a task when caller is owner or assignee.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Task closed.",
        content = @Content(schema = @Schema(implementation = ToDoTaskResponse.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Missing or invalid bearer token.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Caller lacks permission to close this task.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Task not found.",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  })
  public ToDoTaskResponse closeTask(@PathVariable long taskId, Principal principal) {
    long actorUserId = requireAuthenticatedUserId(principal);
    return toResponse(closeToDoTaskUseCase.execute(taskId, actorUserId));
  }

  private static long requireAuthenticatedUserId(Principal principal) {
    if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Authenticated principal is required.");
    }
    try {
      return Long.parseLong(principal.getName());
    } catch (NumberFormatException ex) {
      throw new ResponseStatusException(
          HttpStatus.UNAUTHORIZED, "Authenticated principal must contain numeric user id.");
    }
  }

  private static ToDoTaskResponse toResponse(ToDoTaskView task) {
    return new ToDoTaskResponse(
        task.id(),
        task.ownerId(),
        task.assigneeId(),
        task.title(),
        task.description(),
        task.deadline(),
        task.priority(),
        task.status());
  }
}
