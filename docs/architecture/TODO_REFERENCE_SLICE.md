# ToDo Reference Slice (First Extraction)

Date: 2026-02-19

This document captures the first end-to-end extraction from the legacy monolith into the modern Maven/Spring stack.

## Legacy to Modern Mapping

Legacy sources used for behavior baseline:
- `src/java/hr/fer/zemris/jcms/model/ToDoTask.java`
- `src/java/hr/fer/zemris/jcms/service/ToDoService.java`
- `src/java/hr/fer/zemris/jcms/web/actions/ToDo.java`
- `src/java/hr/fer/zemris/jcms/web/actions/data/ToDoData.java`

Modern implementation:
- Domain:
  - `backend/ferko-domain/src/main/java/hr/fer/zemris/ferko/domain/model/ToDoTask.java`
  - `backend/ferko-domain/src/main/java/hr/fer/zemris/ferko/domain/model/ToDoTaskPriority.java`
  - `backend/ferko-domain/src/main/java/hr/fer/zemris/ferko/domain/model/ToDoTaskStatus.java`
- Application:
  - `backend/ferko-application/src/main/java/hr/fer/zemris/ferko/application/port/ToDoTaskRepository.java`
  - `backend/ferko-application/src/main/java/hr/fer/zemris/ferko/application/usecase/todo/*`
- Infrastructure:
  - `backend/ferko-infrastructure/src/main/java/hr/fer/zemris/ferko/infrastructure/adapter/JdbcToDoTaskRepository.java`
  - `backend/ferko-infrastructure/src/main/java/hr/fer/zemris/ferko/infrastructure/adapter/InMemoryToDoTaskRepository.java` (fallback profile)
- Web/API:
  - `backend/ferko-web-api/src/main/java/hr/fer/zemris/ferko/webapi/controller/ToDoController.java`
  - `backend/ferko-web-api/src/main/java/hr/fer/zemris/ferko/webapi/controller/ToDoExceptionHandler.java`
  - `backend/ferko-web-api/src/main/java/hr/fer/zemris/ferko/webapi/dto/*`

## Implemented Capabilities (MVP)

1. Create ToDo task.
2. List open tasks assigned to a user.
3. List open tasks assigned by an owner to other users.
4. Close task with owner/assignee authorization check.
5. Persist tasks in SQL storage through JDBC adapter and Flyway-managed schema.
6. Persist privileged-action audit trail (`CREATE`, `CLOSE`) with denied-attempt coverage.
7. Generate and version OpenAPI contract with CI-backed contract checks.

## HTTP API (MVP)

- `POST /api/v1/todo/tasks`
- `GET /api/v1/todo/my`
- `GET /api/v1/todo/assigned`
- `POST /api/v1/todo/tasks/{taskId}/close`

Identity model:
- Endpoints derive current user id from authenticated JWT principal (`Principal#getName()` must be numeric user id).
- Request query parameters for caller identity are no longer used.
- ToDo endpoints require `Authorization: Bearer <JWT>`.
- ToDo authorization rules:
  - `GET /api/v1/todo/my`, `GET /api/v1/todo/assigned` require `SCOPE_todo.read` or `ROLE_TODO_READ`.
  - `POST /api/v1/todo/tasks`, `POST /api/v1/todo/tasks/{taskId}/close` require `SCOPE_todo.write` or `ROLE_TODO_WRITE`.

## Test Coverage Added

- Domain invariants:
  - `backend/ferko-domain/src/test/java/hr/fer/zemris/ferko/domain/model/ToDoTaskTest.java`
- Application use cases:
  - `backend/ferko-application/src/test/java/hr/fer/zemris/ferko/application/usecase/todo/ToDoUseCasesTest.java`
- Infrastructure repository:
  - `backend/ferko-infrastructure/src/test/java/hr/fer/zemris/ferko/infrastructure/adapter/InMemoryToDoTaskRepositoryTest.java`
  - `backend/ferko-infrastructure/src/test/java/hr/fer/zemris/ferko/infrastructure/adapter/JdbcToDoTaskRepositoryTest.java`
  - `backend/ferko-infrastructure/src/test/java/hr/fer/zemris/ferko/infrastructure/adapter/JdbcToDoTaskRepositoryPostgresTest.java` (Testcontainers)
- API integration:
  - `backend/ferko-web-api/src/test/java/hr/fer/zemris/ferko/webapi/ToDoControllerTest.java`
  - `backend/ferko-web-api/src/test/java/hr/fer/zemris/ferko/webapi/ToDoOpenApiContractTest.java`

## Architectural Guardrail Check

`./mvnw verify` passes with ArchUnit boundary rules enabled:
- web layer does not directly depend on domain package,
- module dependency constraints remain valid,
- cycle checks pass.

## Next Evolution Steps

1. Extend audit reporting/inspection workflow for administrators.
2. Add release image signing/provenance on top of GHCR semantic/SHA publishing.
3. Expand deployment policy checks for hardened `staging`/`prod` profiles.
