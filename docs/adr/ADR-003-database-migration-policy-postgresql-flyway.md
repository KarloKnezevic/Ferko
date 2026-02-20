# ADR-003: Database Target is PostgreSQL with Flyway-Managed Migrations

- Status: Accepted
- Date: 2026-02-19
- Owners: Ferko modernization track
- Related tickets: Data platform modernization

## Context

Legacy schema evolution relies on implicit/manual changes tied to environment-specific setup. This is difficult to reproduce and increases release risk. A deterministic, versioned migration policy is required for CI/CD and controlled rollouts.

## Decision

PostgreSQL is the target operational database. Schema changes are managed only via Flyway versioned migrations. Every deployment environment must reconstruct schema state from migration scripts.

## Alternatives considered

1. Continue with MySQL and ad-hoc migration scripts
2. PostgreSQL with manual DDL management
3. PostgreSQL with Flyway versioned migrations (selected)

Ad-hoc and manual approaches were rejected because they weaken reproducibility and auditability.

## Consequences

- Positive:
  - Deterministic schema evolution across environments.
  - Easier rollback/rehearsal planning.
  - Better CI/CD integration.
- Negative:
  - Upfront migration engineering cost from legacy schema.
  - Need for disciplined migration review process.

## Follow-up actions

1. Define baseline PostgreSQL schema and migration naming policy.
2. Add migration verification to CI and staging rehearsals.
3. Produce reconciliation checks for MySQL to PostgreSQL data migration.
