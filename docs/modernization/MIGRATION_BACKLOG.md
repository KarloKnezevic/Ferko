# Ferko Migration Backlog (Execution)

This backlog translates the transformation plan into implementation-ready epics.

Status legend:
- `P0` critical foundation
- `P1` high priority
- `P2` medium priority

## Execution Progress Snapshot (2026-02-19)

- [x] Maven multi-module foundation and Java 21 policy enforcement
- [x] CI quality gates (Spotless, Checkstyle, JaCoCo) and dependency security scanning
- [x] Executable architecture boundary checks (ArchUnit) wired into `./mvnw verify`
- [x] ADR baseline created with three accepted modernization decisions
- [x] Legacy architecture mapping with extraction candidate ranking
- [x] First domain extraction reference slice (`ToDo`) implemented end-to-end
- [x] `ToDo` persistence upgraded to Flyway-managed SQL repository (JDBC adapter)
- [x] PostgreSQL Testcontainers integration coverage added for `ToDo` JDBC repository path
- [x] `ToDo` endpoints switched from query-param identity to authenticated principal identity
- [x] Spring Security JWT authorization policy added (`todo.read` / `todo.write` scopes + role mapping)
- [x] Legacy characterization parity tests added for `ToDo` ordering/filtering/permission edge cases
- [x] Persistence-backed audit logging added for privileged `ToDo` actions, including denied attempts
- [x] OpenAPI contract generation and contract tests added for the `ToDo` slice
- [x] Containerization baseline (`Dockerfile` + `docker-compose.yml`) for migrated modules
- [x] CI container smoke gate added (`docker compose` startup + readiness/ping checks)
- [x] CI container security gate added (image build + Trivy vulnerability scan)
- [x] GHCR release image publishing workflow added (semantic + immutable SHA tags, multi-arch)
- [x] Deployment hardening profiles added (`staging` / `prod` secure JWT decoder defaults)
- [ ] Next priority: release provenance/signing and staged deployment policy enforcement

## Epic E01 (P0): Program Governance and Architecture Decisions
Goal: establish execution governance, ADR workflow, and delivery controls.

Tasks:
- Create ADR template and initial ADR set.
- Define branch/release strategy.
- Establish migration readiness checklist.

Definition of done:
- ADR repository active with at least 3 approved decisions.
- Release policy documented and used by team.

## Epic E02 (P0): Maven + Java 21 Build Foundation
Goal: replace Ant with Maven and lock Java 21 toolchain.

Tasks:
- Create parent POM and module structure.
- Migrate compile/test/package lifecycle.
- Introduce dependency management section (BOM where applicable).
- Move vendored jars to repository-managed dependencies where possible.

Definition of done:
- `./mvnw verify` runs successfully in CI.
- No ad-hoc Ant-only steps required for standard build.

Dependencies:
- E01

## Epic E03 (P0): CI Quality Pipeline
Goal: enforce repeatable quality gates.

Tasks:
- Add CI workflow for build/test/static checks.
- Add coverage reporting and thresholds.
- Add dependency vulnerability scanning.

Definition of done:
- CI required checks block merges on failure.
- Artifacts are versioned and reproducible.

Dependencies:
- E02

## Epic E04 (P1): Spring Boot Strangler Entry Module
Goal: establish modern runtime path without breaking legacy behavior.

Tasks:
- Bootstrap Spring Boot app.
- Add actuator, structured logging, profile-based config.
- Implement first migrated endpoint from legacy functionality.

Definition of done:
- One production-safe feature path served by modern module.
- Monitoring and logs available for that path.

Dependencies:
- E02, E03

## Epic E05 (P1): Persistence and Flyway Governance
Goal: remove implicit schema mutation and introduce versioned migrations.

Tasks:
- Define PostgreSQL schema baseline.
- Build Flyway migration chain.
- Implement migration verification jobs.

Definition of done:
- Schema can be recreated from scratch from migration scripts.
- Migration job validated in CI/staging.

Dependencies:
- E02, E03

## Epic E06 (P1): Data Migration MySQL -> PostgreSQL
Goal: migrate data with reconciliation and rollback capability.

Tasks:
- Export/import tooling.
- Reconciliation checks per table/domain.
- Dress rehearsal migrations on production-like data.

Definition of done:
- Reconciliation report signed off.
- Rollback drill executed successfully.

Dependencies:
- E05

## Epic E07 (P1): Authentication and RBAC Modernization
Goal: replace legacy auth entry points with Spring Security and OIDC.

Tasks:
- Define identity provider integration.
- Map legacy roles to RBAC policy.
- Implement login and token validation flows.
- Add audit logging for auth/privileged actions.

Definition of done:
- End-to-end auth tests pass.
- Legacy auth disabled for migrated endpoints.

Dependencies:
- E04

## Epic E08 (P1): API Program and OpenAPI Docs
Goal: establish API-first contracts for migrated capabilities.

Tasks:
- Define REST conventions.
- Publish OpenAPI spec for migrated modules.
- Add API contract tests.

Definition of done:
- OpenAPI spec generated and versioned.
- Contract tests run in CI.

Dependencies:
- E04

## Epic E09 (P2): UI Modernization Track
Goal: replace high-value JSP workflows with modern UI.

Tasks:
- Identify top user workflows.
- Build modern frontend track (SPA or Thymeleaf SSR modernization).
- Integrate with modern auth and API.

Definition of done:
- At least 3 high-traffic workflows migrated.
- Legacy JSP usage reduced and measured.

Dependencies:
- E07, E08

## Epic E10 (P1): Containerization and Local Stack
Goal: fully reproducible local and staging environment.

Tasks:
- Multi-stage Dockerfile.
- Docker Compose with app + postgres (+ optional redis/mailhog).
- Non-root image hardening.

Definition of done:
- One-command local startup for full stack.
- Security scan of image is part of CI.

Dependencies:
- E04, E05

## Epic E11 (P1): Observability and SRE Readiness
Goal: operational visibility and incident response readiness.

Tasks:
- Micrometer metrics.
- Prometheus scrape endpoint.
- Dashboards and alerts.
- Health and readiness probes.

Definition of done:
- Dashboards and alerts validated in staging.
- Runbook for incident triage available.

Dependencies:
- E04

## Epic E12 (P0): Documentation in English
Goal: complete and maintainable English documentation set.

Tasks:
- Rewrite admin/developer docs.
- Provide deployment and backup/restore guides.
- Keep changelog and release notes policy.

Definition of done:
- Docs reviewed and sufficient for handover.
- No critical runbook gaps.

Dependencies:
- E01 (starts early, completes at program end)

## First 6 Sprint Proposal

Sprint 1:
- E01 start + E02 start

Sprint 2:
- E02 finish + E03 start

Sprint 3:
- E03 finish + E04 start

Sprint 4:
- E04 finish + E05 start + E12 ongoing

Sprint 5:
- E05 finish + E06 start + E07 start

Sprint 6:
- E06/E07 continue + E08 start + E10 start
