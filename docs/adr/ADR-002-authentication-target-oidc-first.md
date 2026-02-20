# ADR-002: Target Authentication Model is OIDC-First with Spring Security

- Status: Accepted
- Date: 2026-02-19
- Owners: Ferko modernization track
- Related tickets: Security workstream kickoff

## Context

Legacy authentication includes POP3/LDAP/custom providers and local credential handling patterns that are hard to maintain and audit. The modernized system needs standardized identity integration, stronger security posture, and cleaner role-based authorization controls.

## Decision

Authentication target is OIDC/OAuth2-first via Spring Security, with optional LDAP bridge where institutional constraints require it.

Authorization target is endpoint and service-level RBAC with auditable decisions.

## Alternatives considered

1. Preserve legacy provider model
2. LDAP-only target
3. OIDC-first with optional LDAP integration (selected)

Legacy preservation was rejected due to security and maintenance burden.
LDAP-only was rejected because it limits portability and modern federation patterns.

## Consequences

- Positive:
  - Standards-based identity and token validation.
  - Reduced custom auth code.
  - Better auditability and interoperability.
- Negative:
  - Initial integration complexity with identity provider setup.
  - Potential migration period with parallel auth validation.

## Follow-up actions

1. Define canonical role model and mapping from legacy roles.
2. Add authentication and authorization integration tests.
3. Plan phased cutover and disable legacy auth paths per endpoint group.
