# Installation Guide

## Recommended Runtime (Docker)

### Prerequisites

- Docker Desktop / Docker Engine with Compose support
- 4 GB RAM minimum for containers

### Start

```bash
./scripts/dev-up.sh
```

### Verify

- UI: `http://localhost:8080`
- Health: `http://localhost:8080/actuator/health`
- API docs: `http://localhost:8080/swagger-ui/index.html`

### Stop

```bash
./scripts/dev-down.sh
```

### Hard reset (remove database volume)

```bash
./scripts/dev-reset.sh
```

## Non-Docker Local Build/Test

### Prerequisites

- Java 21+

### Validate build

```bash
./mvnw -B -ntp verify
```

## Profile Behavior

- Default profile: local-friendly baseline, seeded bootstrap enabled.
- `docker`: PostgreSQL-backed runtime for local container deployment.
- `staging`/`prod`: hardened profile (dev token disabled, no HMAC fallback).

In `staging`/`prod`, you must provide one of:

- `FERKO_OIDC_ISSUER_URI`
- `FERKO_OIDC_JWK_SET_URI`

Otherwise startup fails by design.

## Apple Silicon (M1/M2/M3)

Supported directly. Multi-arch images and docker configuration include ARM64.

## Legacy Install Notes

If you need the historical Ant/Tomcat deployment flow, see:

- `docs/legacy/HOW_TO_INSTALL_EN.md`
