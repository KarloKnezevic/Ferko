# Module Dependency Rules

This document defines Phase 1 baseline boundaries for the new Maven module structure.
Rules are machine-enforced through ArchUnit tests in:

- `/Users/karloknezevic/Desktop/Ferko/backend/ferko-architecture-tests/src/test/java/hr/fer/zemris/ferko/architecture/ModuleDependencyRulesTest.java`

## Allowed direction (high-level)

```text
domain <- application <- infrastructure
                      <- security
domain <- application <- webapi
```

Interpretation:
- `domain` is the core and must not depend on outer layers.
- `application` orchestrates use cases and must not depend on infrastructure/web/security details.
- `infrastructure` can depend on `application` (and transitively `domain`) but not on `webapi` or `security`.
- `security` can depend on `application` (and transitively `domain`) but not on `webapi` or `infrastructure`.
- `webapi` is an interface adapter and must not depend directly on `domain`.

## Enforced fitness rules

1. `domain` does not depend on `application`, `infrastructure`, `security`, or `webapi`.
2. `application` does not depend on `infrastructure`, `security`, or `webapi`.
3. `infrastructure` does not depend on `webapi` or `security`.
4. `security` does not depend on `webapi` or `infrastructure`.
5. `webapi` does not depend directly on `domain`.
6. Core packages are cycle-free.

## How to run

```bash
./mvnw -B -ntp verify
```

`verify` executes ArchUnit tests via Surefire, so CI will fail on boundary violations.
