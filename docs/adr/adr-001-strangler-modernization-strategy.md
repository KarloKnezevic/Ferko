# ADR-001: Use Strangler Strategy for Ferko Modernization

- Status: Accepted
- Date: 2026-02-19
- Owners: Ferko modernization track
- Related tickets: Phase 1 bootstrap

## Context

Ferko currently runs as a legacy monolith with tightly coupled Struts/JSP logic. A full rewrite would require a long freeze and high regression risk because critical domain behavior is distributed across actions, services, JSPs, and legacy configuration.

## Decision

Modernization will follow a strangler strategy:
- build a modern Spring Boot path in parallel,
- migrate features slice-by-slice,
- keep coexistence and rollback capability during migration.

## Alternatives considered

1. Big-bang rewrite
2. In-place framework upgrade inside monolith
3. Strangler strategy (selected)

Big-bang rewrite was rejected due to outage/regression risk and delayed value delivery.
In-place upgrade was rejected due to deep framework coupling and hard-to-test blast radius.

## Consequences

- Positive:
  - Lower production risk through incremental delivery.
  - Earlier value from partial migrations.
  - Clear rollback paths per migrated slice.
- Negative:
  - Temporary dual-stack complexity.
  - Additional integration work between legacy and modern modules.

## Follow-up actions

1. Enforce module boundaries in Maven/Spring Boot track.
2. Define migration sequencing by workflow risk/value.
3. Add characterization tests before migrating each high-risk feature.
