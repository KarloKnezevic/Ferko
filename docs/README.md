# FERKO documentation

Documentation for the modernized FER academic portal. Start with the root
[README](../README.md) for an overview, then dive into the guides below.

## Getting started

- [Quick start](./getting-started/quickstart.md) — run the fully seeded portal with one command
- [Installation](./getting-started/installation.md) — prerequisites and setup
- [Data initialization](./getting-started/data-initialization.md) — how the demo dataset is seeded

## Using FERKO

- [User guide](./user-guide.md) — every feature, organized by role

## Architecture and engineering

- [Architecture](./architecture/architecture.md) — hexagonal layers, modules, boundaries, request flow
- [Domain model](./architecture/domain-model.md) — entities, ER diagram, Flyway migrations
- [Scheduling engine](./architecture/scheduling-engine.md) — Čupić's evolutionary models, optimizers, problems
- [Security model](./architecture/security-model.md) — authentication chains, roles, row-level access
- [Contributing](./architecture/contributing.md) — the vertical-slice recipe, verification, conventions

## Operations

- [Production deployment](./operations/production-deployment.md) — production compose, profile, environment, topology
- [Containerization](./operations/containerization-baseline.md)
- [Releases](./operations/releases.md) — cutting a release and the published GHCR image

## API contracts

- [OpenAPI YAML](./api/openapi.yaml)
- [OpenAPI JSON](./api/openapi.json)

## Decision records

- [ADR-001 — modernization strategy](./adr/adr-001-strangler-modernization-strategy.md)
- [ADR-002 — authentication, OIDC-first](./adr/adr-002-authentication-target-oidc-first.md)
- [ADR-003 — database policy, PostgreSQL and Flyway](./adr/adr-003-database-migration-policy-postgresql-flyway.md)

## Project history

The [modernization](./modernization/) folder keeps the transformation plan, baseline audit,
migration backlog and dependency triage logs from the modernization effort.
