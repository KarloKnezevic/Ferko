# Ferko Radical Modernization Plan

## 1. Program Objective
Transform Ferko from a legacy Java/Struts monolith into a secure, testable, container-ready, long-lived platform on Java 21 with modern operational practices.

Target outcomes:
- Java 21 LTS runtime
- Spring Boot 3+ architecture
- PostgreSQL + Flyway-managed schema
- OAuth2/OIDC-capable authentication and RBAC
- CI/CD with repeatable builds and quality gates
- Full English documentation for admin, developer, API users

## 2. Transformation Strategy

### Strategy choice
Use a **strangler** approach (incremental replacement) rather than a full rewrite.

Why:
- Repository has 1227 Java files and 325 JSPs.
- Legacy feature density is high and business rules are deeply embedded.
- Incremental replacement preserves service continuity and lowers regression risk.

### Guardrails
- No unplanned production data model breaks.
- Every phase has explicit entry/exit criteria.
- Rollback path must exist for every production migration.
- Security upgrades are treated as functional requirements, not optional hardening.

## 3. Target Architecture

```text
ferko/
  backend/
    ferko-domain/
    ferko-application/
    ferko-infrastructure/
    ferko-web-api/
    ferko-security/
    ferko-architecture-tests/
  frontend/               # optional SPA track
  database/
    migrations/
    seed/
  ops/
    docker/
    compose/
    ci/
  docs/
    adr/
    architecture/
    admin-guide/
    developer-guide/
    api/
```

Design principles:
- Domain-centered modeling
- Clean boundaries between domain/application/infrastructure
- Explicit dependency inversion
- REST-first API contracts
- Backward-compatible rollout where necessary

## 4. Workstreams

### WS-A: Build + Runtime Modernization
- Ant to Maven migration with deterministic dependency management.
- Java 21 migration with compiler/toolchain locking.
- Removal of vendored jar dependency model.

### WS-B: Architecture and Code Refactoring
- Extract domain logic from Struts actions/services.
- Introduce Spring-managed DI and package modularization.
- Replace static/global mutable state patterns.

### WS-C: Auth and Security Modernization
- Replace JAAS/Tomcat realm-centric auth with Spring Security.
- Introduce OAuth2/OIDC integration (and optional LDAP bridge).
- Add JWT token strategy for API-first usage.
- Implement RBAC policies at endpoint/service layers.

### WS-D: Data Platform Modernization
- MySQL-to-PostgreSQL migration.
- Flyway versioned migrations and seeded bootstrap datasets.
- Compatibility verification and dual-run comparison for critical queries.

### WS-E: DevOps + Operations
- Multi-stage Docker image, non-root runtime.
- Docker Compose for local reproducible stack (app + postgres + optional redis + mailhog).
- CI/CD for build, tests, static analysis, image publishing, tag/release workflows.

### WS-F: Quality Engineering
- JUnit 5 + Mockito unit tests.
- Spring integration tests with Testcontainers.
- API contract tests and auth flow tests.
- Coverage targets and mutation/regression checks for core domains.

### WS-G: Documentation and Enablement
- Full English docs (admin + developer + API/OpenAPI).
- ADR records for major technical decisions.
- Runbooks for operations, backup/restore, and incident response.

## 5. Phased Delivery Plan

### Phase 0: Program Setup (2 weeks)
Deliverables:
- Baseline audit
- ADR template + governance
- Environments matrix and risk register
- Branching/release policy

Exit criteria:
- Stakeholder-approved transformation scope
- Signed-off non-functional requirements (security, SLOs, compliance)

### Phase 1: Build and Dependency Migration (4-6 weeks)
Deliverables:
- Maven multi-module structure
- Build parity with legacy artifacts (where required)
- Dependency inventory + CVE triage
- CI pipeline building the code on Java 21 toolchain

Exit criteria:
- `./mvnw verify` green in CI
- No dependency pulled from ad-hoc local jar directories without declaration

### Phase 2: Spring Boot Foundation + Strangler Entry (6-8 weeks)
Deliverables:
- New Spring Boot service shell
- Initial domain/application abstractions
- First migrated feature slice exposed via REST API
- Structured logging and basic actuator endpoints

Exit criteria:
- First feature in production through modern stack
- Legacy and modern paths can coexist safely

### Phase 3: Data Migration and Schema Governance (6-10 weeks)
Deliverables:
- PostgreSQL schema design
- Flyway migration chain (baseline + incremental)
- Data migration scripts and validation reports
- Seed/demo datasets

Exit criteria:
- Data reconciliation within agreed tolerance
- Rollback + restore drill completed

### Phase 4: Authentication and Authorization Refactor (5-8 weeks)
Deliverables:
- Spring Security integration
- OIDC/OAuth2 provider integration
- RBAC policy mapping from legacy roles
- Auth audit logging

Exit criteria:
- Successful end-to-end login/authorization tests
- Legacy auth paths disabled for migrated endpoints

### Phase 5: UI and API Expansion (8-14 weeks)
Deliverables:
- Incremental migration from JSP/Struts pages to modern web/API patterns
- OpenAPI documentation for released endpoints
- Optional SPA frontend foundation

Exit criteria:
- Majority of high-value workflows served by modern API/web path
- Legacy UI reduced to low-priority or not-yet-migrated features only

### Phase 6: Operations Hardening and Observability (3-5 weeks)
Deliverables:
- Production-grade Docker image and compose stack
- Metrics with Micrometer + Prometheus endpoint
- Alerting and dashboard templates
- Security baseline checks (headers, CSRF, cookies, upload hardening)

Exit criteria:
- SLO dashboards and health probes operational
- CI/CD release pipeline supports versioned deployments

### Phase 7: Documentation and Decommission (2-4 weeks)
Deliverables:
- Complete English admin/developer/API docs
- Final migration report
- Legacy component decommission plan

Exit criteria:
- Modern platform declared primary
- Legacy runtime decommissioned or isolated by approved policy

## 6. Testing and Quality Gates

Mandatory gates per merge to main modernization branch:
- Unit + integration tests pass
- Static analysis pass (Spotless, Checkstyle/PMD, Sonar)
- Security scan pass (dependencies + container image)
- OpenAPI checks for API contract changes

Release gates:
- Smoke tests in staging
- Migration scripts dry-run + verification
- Auth regression suite pass
- Performance sanity baseline (P95 latency and error budget)

## 7. Security Baseline (Target)
- HTTPS-only ingress
- HSTS and secure headers
- CSRF protections for browser flows
- Secure cookie flags (`HttpOnly`, `Secure`, `SameSite`)
- Strong password hashing (bcrypt/argon2) where local credentials remain
- Account lockout/rate limiting
- Audit trail for auth and privileged actions
- File upload scanning/validation and storage isolation

## 8. DevOps Blueprint

CI stages:
1. Build and dependency cache warm-up
2. Unit tests
3. Integration tests with Testcontainers
4. Static analysis and quality gate
5. Docker image build + vulnerability scan
6. Publish tagged image

CD stages:
1. Deploy to staging
2. Run smoke + migration verification
3. Manual approval gate for production
4. Deploy with rollback hooks

## 9. Documentation Deliverables

Required docs:
- `docs/admin-guide/*`
- `docs/developer-guide/*`
- `docs/api/openapi.yaml`
- `docs/architecture/*`
- `docs/adr/*`

Documentation policy:
- English-only for newly created docs
- Every major migration decision captured as ADR
- No release without updated runbook sections

## 10. KPIs and Success Criteria

Engineering KPIs:
- Build reproducibility: 100% CI-based release builds
- Test coverage: minimum 80% on newly migrated core modules
- Lead time trend improving over 3 release cycles
- Critical CVEs: zero in production artifacts

Platform KPIs:
- Availability target defined and monitored
- P95 latency baseline and improvement targets by endpoint class
- Mean-time-to-recover reduced through standardized runbooks

## 11. Critical Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Hidden domain logic in Struts actions/JSPs | Regression risk | Characterization tests before migration of each feature slice |
| Data migration edge cases | Data integrity risk | Rehearsal migrations + checksum-based reconciliation |
| Auth migration complexity | Access/security incidents | Parallel auth validation and phased endpoint cutover |
| Underestimated scope | Schedule slippage | Strict prioritization of high-value workflows first |
| Team context gaps | Delivery friction | ADR discipline + paired migration on critical modules |

## 12. Immediate Next Sprint (Execution Starter)

1. Stand up Maven parent project in a modernization branch.
2. Build dependency BOM and identify replace/remove candidates.
3. Implement first Spring Boot module with health endpoint and shared config.
4. Capture first 3 ADRs:
- Strangler strategy
- Auth target (OIDC-first)
- Database migration policy (Flyway + PostgreSQL)
5. Select and migrate one low-risk feature slice end-to-end as reference.

## 13. Current Execution Status (2026-02-19)

Completed:
1. Maven parent + backend multi-module skeleton and wrapper-based build (`./mvnw`).
2. Spring Boot entry module (`ferko-web-api`) with first health/ping path.
3. CI quality gates: Spotless, Checkstyle, JaCoCo, dependency review, OWASP dependency scanning.
4. First three ADRs:
- `ADR-001-strangler-modernization-strategy.md`
- `ADR-002-authentication-target-oidc-first.md`
- `ADR-003-database-migration-policy-postgresql-flyway.md`
5. Architecture fitness tests enforcing module dependency boundaries in CI.
6. Legacy architecture map and extraction candidates documented in `docs/architecture/LEGACY_ARCHITECTURE_MAP.md`.
7. First reference extraction slice (`ToDo`) implemented with domain/application/infrastructure/API layers.
8. `ToDo` repository moved from in-memory to Flyway-managed SQL persistence (JDBC adapter).
9. PostgreSQL Testcontainers integration test added for `ToDo` JDBC repository path.
10. `ToDo` API identity switched from query params to authenticated principal.
11. Spring Security JWT scope/role policy added for `ToDo` endpoints.
12. Characterization parity tests added for `ToDo` ordering/filtering/permission edge cases.
13. Persistence-backed audit logging added for privileged `ToDo` actions, including denied attempts.
14. OpenAPI generation and contract tests added for `ToDo`, with versioned spec in `docs/api/openapi.yaml`.
15. Containerization baseline delivered for modern backend slice:
- root `Dockerfile` (multi-stage build, Java 21 runtime, non-root execution)
- root `docker-compose.yml` (`ferko-app` + PostgreSQL + optional Redis/Mailhog)
- `application-docker.yml` profile for PostgreSQL runtime defaults and readiness probes.
16. CI container security gate added:
- image build from `Dockerfile`
- Trivy vulnerability scan with HIGH/CRITICAL fail threshold.
17. CI container smoke gate added:
- `docker compose up` for `ferko-app` + `postgres`
- readiness check on `/actuator/health` and smoke check on `/api/v1/ping`.
18. GHCR release image workflow added:
- trigger on semantic git tags (`v*.*.*`)
- publishes semantic tags and immutable SHA tags
- pushes multi-arch images (`linux/amd64`, `linux/arm64`).
19. Deployment hardening profiles added:
- `application-staging.yml` and `application-prod.yml`
- disable HMAC JWT fallback decoder for non-local environments.

Next:
1. Add release provenance/signing for published images.
2. Add staged deployment runtime policy checks and secret-source integration.
