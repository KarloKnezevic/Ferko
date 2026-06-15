# Containerization Baseline (Phase 1)

Date: 2026-02-19

This document defines the first containerized runtime baseline for the migrated Ferko backend slice (`ferko-web-api`).

## Delivered Assets

- `Dockerfile` (root): multi-stage Maven build and Java 21 runtime image.
- `docker-compose.yml` (root): local stack with:
  - `ferko-app`
  - `postgres`
  - optional `redis` (`--profile optional`)
  - optional `mailhog` (`--profile optional`)
- `backend/ferko-web-api/src/main/resources/application-docker.yml`: Docker profile defaults for PostgreSQL and health probes.

## Security Baseline

- Runtime container uses a dedicated non-root user (`uid=10001`, `gid=10001`).
- Build and runtime images are separated (no Maven tooling in runtime image).
- CI validates image vulnerabilities with Trivy and fails on HIGH/CRITICAL findings.
- Release image pipeline publishes multi-arch images (`linux/amd64`, `linux/arm64`) to support Intel and Apple Silicon hosts.

## Local Usage

Start:

```bash
docker compose up -d --build
```

Start with optional services:

```bash
docker compose --profile optional up -d --build
```

Stop:

```bash
docker compose down
```

Reset database volume:

```bash
docker compose down -v
```

Smoke checks:

```bash
curl -s http://localhost:8080/api/v1/ping
curl -s http://localhost:8080/actuator/health
```

## Environment Variables

- `FERKO_DB_URL` (default `jdbc:postgresql://postgres:5432/ferko`)
- `FERKO_DB_USERNAME` (default `ferko`)
- `FERKO_DB_PASSWORD` (default `ferko`)
- `FERKO_DB_DRIVER` (default `org.postgresql.Driver`)
- `FERKO_DB_NAME` (default `ferko`)
- `FERKO_JWT_HMAC_SECRET` (development default provided in compose; override in non-local environments)

## CI Integration

Workflow: `.github/workflows/maven-phase1.yml`

Container gates:
- Run compose smoke startup (`ferko-app` + `postgres`) with readiness/ping checks.
- Build Docker image from repository root `Dockerfile`.
- Scan built image with Trivy.
- Upload SARIF scan report artifact.

## Next Hardening Targets

1. Runtime secret handling policy (external secret source, no fallback secret defaults).
2. Image signing/provenance and staged deployment smoke verification.
3. Container runtime policy checks (seccomp/cap-drop/read-only fs where applicable).

## Apple Silicon Notes

- Ferko release images support `linux/arm64`.
- If Docker Desktop reports Intel-only requirements on macOS M1/M2:
  1. Install/update Docker Desktop for Apple Silicon.
  2. If a legacy x86-only dependency is unavoidable, use emulation:
     - `export DOCKER_DEFAULT_PLATFORM=linux/amd64`
     - then run `docker compose up -d --build`

This emulation path is slower and should be considered temporary.
